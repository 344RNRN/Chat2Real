package admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Bean.EventReportBean;
import DAO.EventReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 終了レポート（アンケート）画面（画面ID：EventReport）
 *
 * ログインチェック・イベント状態のチェックはしない。表示するもの：
 *   1. セッションに eventParticipantId があれば、その参加者のレポート
 *   2. 無ければ、いちばん最近作られたイベントの、いちばん最近の参加者のレポート
 *   3. そのイベントに参加者がいなければ、質問だけの確認用（送信はできない）
 *
 * GET  … 表示
 * POST … アンケートを登録 → GET に戻して「送信しました」を表示
 */
@WebServlet({ "/participant/EventReport.action", "/participant/SurveyAnswer.action" })
public class EventReportAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 参加者の event_participant_id を入れているセッションのキー */
    private static final String SESSION_PARTICIPANT = "eventParticipantId";

    private static final String JSP = "/participants/eventReport.jsp";
    private static final int MAX_ANSWER = 200;
    private static final int MAX_COMPLAINT = 500;

    // =========================================================
    // GET：画面表示
    // =========================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EventReportBean report = loadReport(request);
        if (report == null) {
            forwardEmpty(request, response, "イベントがまだありません。先にイベントを作成してください");
            return;
        }
        forwardToReport(request, response, report, null);
    }

    // =========================================================
    // POST：⑪ アンケートを送信
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        EventReportBean report = loadReport(request);
        if (report == null) {
            forwardEmpty(request, response, "イベントがまだありません。先にイベントを作成してください");
            return;
        }

        // 参加者がいない確認用の画面では登録できない（回答は参加者に紐づくため）
        if (report.getEventParticipantId() == 0) {
            forwardToReport(request, response, report,
                    "このイベントにはまだ参加者がいないため、送信できません（表示の確認用です）");
            return;
        }

        // 回答済みならそのまま画面へ（「送信しました」が表示される）
        if (report.isAnswered()) {
            redirectToReport(request, response);
            return;
        }

        // ---------- 入力チェックしながら回答を集める ----------
        List<EventReportBean.Answer> answers = new ArrayList<>();
        String error = readAnswers(request, report, answers);
        if (error != null) {
            forwardToReport(request, response, report, error);
            return;
        }

        // ---------- 登録 ----------
        try {
            new EventReportDAO().saveAnswers(report.getEventParticipantId(), answers);
            System.out.println("[EventReport] 回答を登録しました：event_participant_id = "
                    + report.getEventParticipantId());
        } catch (SQLException e) {
            e.printStackTrace();
            forwardToReport(request, response, report, "送信に失敗しました。もう一度お試しください");
            return;
        }

        // 送信完了画面（13-2）へ（再読み込みで二重送信されないようにリダイレクト）
        response.sendRedirect(request.getContextPath() + "/participant/SurveyComplete.action");
    }

    // =========================================================
    // どの参加者（またはイベント）を表示するか決める
    // =========================================================
    private EventReportBean loadReport(HttpServletRequest request) throws ServletException {
        EventReportDAO dao = new EventReportDAO();
        try {
            // 1. セッションの参加者
            Long epId = getParticipantId(request);
            if (epId != null) {
                EventReportBean r = dao.findReport(epId);
                if (r != null) {
                    return r;
                }
            }

            // 2. いちばん最近のイベント
            Long eventId = dao.findLatestEventId();
            if (eventId == null) {
                return null;
            }

            Long latestEp = dao.findLatestParticipantId(eventId);
            if (latestEp != null) {
                return dao.findReport(latestEp);
            }

            // 3. 参加者がいなければ確認用
            return dao.findPreview(eventId);

        } catch (SQLException e) {
            throw new ServletException("終了レポートを読み込めませんでした", e);
        }
    }

    // =========================================================
    // 回答の読み取りとチェック
    // =========================================================
    private String readAnswers(HttpServletRequest request, EventReportBean report,
                               List<EventReportBean.Answer> answers) {

        // 運営者の質問（answer_<question_id>）
        for (EventReportBean.Question q : report.getQuestions()) {
            String value = trim(request.getParameter("answer_" + q.getQuestionId()));

            if (value.isEmpty()) {
                if (q.isRequired()) {
                    return "「" + q.getQuestionText() + "」に回答してください";
                }
                continue;   // 任意で未回答なら登録しない
            }

            if (q.isSelect()) {
                if (!q.getOptionList().contains(value)) {
                    return "「" + q.getQuestionText() + "」の選択肢から選んでください";
                }
            } else if (value.length() > MAX_ANSWER) {
                return "「" + q.getQuestionText() + "」は" + MAX_ANSWER + "文字以内で入力してください";
            }

            answers.add(new EventReportBean.Answer(q.getQuestionId(), value));
        }

        // 不満点（任意）
        String complaint = trim(request.getParameter("complaint"));
        if (complaint.length() > MAX_COMPLAINT) {
            return "不満点は" + MAX_COMPLAINT + "文字以内で入力してください";
        }
        if (!complaint.isEmpty() && report.getComplaintQuestionId() > 0) {
            answers.add(new EventReportBean.Answer(report.getComplaintQuestionId(), complaint));
        }

        // ⑩ 満足度（必須・1～5）
        int stars;
        try {
            stars = Integer.parseInt(trim(request.getParameter("satisfaction")));
        } catch (NumberFormatException e) {
            return "満足度（★）を選んでください";
        }
        if (stars < 1 || stars > 5) {
            return "満足度（★）を選んでください";
        }
        if (report.getRatingQuestionId() == 0) {
            return "このイベントの満足度アンケートが見つかりません。運営者にお問い合わせください";
        }
        answers.add(new EventReportBean.Answer(report.getRatingQuestionId(), String.valueOf(stars)));

        return null;
    }

    // =========================================================
    // 共通
    // =========================================================

    /** eventReport.jsp が受け取る名前で request に入れる */
    private void forwardToReport(HttpServletRequest request, HttpServletResponse response,
                                 EventReportBean report, String error)
            throws ServletException, IOException {

        request.setAttribute("eventName", report.getEventName());
        request.setAttribute("meetupCount", report.getMeetupCount());
        request.setAttribute("questions", report.getQuestions());
        request.setAttribute("answered", report.isAnswered());
        if (error != null) {
            request.setAttribute("errorMessage", error);
        }
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    /** イベントが1件も無いとき */
    private void forwardEmpty(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    private void redirectToReport(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/participant/EventReport.action");
    }

    private Long getParticipantId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object obj = session.getAttribute(SESSION_PARTICIPANT);
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}