package participant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import Bean.ExchangeRequestBean;
import Bean.MeetupBean;
import Bean.ProfileBean;
import DAO.ExchangeRequestDAO;
import DAO.MeetupDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 交流結果登録画面（画面設計書 11_交流結果登録 / イベント処理27）
 * 例：/participants/meetResult?exchangeRequestId=9001
 *
 * t002_meetup.meetup_result に保存する値
 *   会って話せた → 「成立」（EventReportDAO / EventDashboardDAO の MEETUP_SUCCESS と同じ）
 *   会えなかった → 「不成立」、理由を選んだときは「不成立：時間がなかった」のように保存
 */
@WebServlet("/participants/meetResult")
public class MeetResultAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static final String RESULT_MET = "成立";
    static final String RESULT_NOT_MET = "不成立";

    // 会えなかった理由（画面設計書 No.4 の選択肢）
    private static final List<String> REASONS =
            List.of("見つけられなかった", "時間がなかった", "その他");

    private static final String JSP = "/participants/meetResult.jsp";

    // ===== 画面を表示する =====
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        long exchangeRequestId =
                MeetupAction.toLong(request.getParameter("exchangeRequestId"));
        long myId = MeetupAction.getMyEventParticipantId(request);

        long partnerId = findPartnerId(exchangeRequestId, myId);

        if (partnerId > 0) {
            setPartner(request, partnerId, exchangeRequestId);

            // もう登録してあれば、その結果を選んだ状態で表示する
            MeetupBean saved = findMyResult(myId, exchangeRequestId);

            if (saved != null) {
                String value = saved.getMeetupResult();

                if (RESULT_MET.equals(value)) {
                    request.setAttribute("savedResult", "met");
                } else {
                    request.setAttribute("savedResult", "notMet");

                    int colon = value.indexOf('：');
                    if (colon >= 0) {
                        request.setAttribute("savedReason", value.substring(colon + 1));
                    }
                }
            }
        }

        request.getRequestDispatcher(JSP).forward(request, response);
    }

    // ===== 登録する =====
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        long exchangeRequestId =
                MeetupAction.toLong(request.getParameter("exchangeRequestId"));
        String result = request.getParameter("result");
        String reason = request.getParameter("reason");

        long myId = MeetupAction.getMyEventParticipantId(request);
        long partnerId = findPartnerId(exchangeRequestId, myId);

        // 自分の交流申請でなければ登録しない
        if (partnerId == 0) {
            response.sendRedirect(request.getContextPath() + "/participants/meetResult");
            return;
        }

        // 1. 未選択ならエラー（イベント処理27）
        if (!"met".equals(result) && !"notMet".equals(result)) {
            request.setAttribute("errorMessage", "会って話せたかどうかを選択してください。");
            setPartner(request, partnerId, exchangeRequestId);
            request.getRequestDispatcher(JSP).forward(request, response);
            return;
        }

        // 2. 保存する値（理由は「会えなかった」のときだけ）
        String value;
        if ("met".equals(result)) {
            value = RESULT_MET;
        } else if (REASONS.contains(reason)) {
            value = RESULT_NOT_MET + "：" + reason;
        } else {
            value = RESULT_NOT_MET;
        }

        // 3. 登録（もう登録してあれば上書き）
        MeetupDAO dao = new MeetupDAO();
        MeetupBean saved = findMyResult(myId, exchangeRequestId);

        boolean ok;
        if (saved != null) {
            saved.setMeetupResult(value);
            ok = dao.updateMeetup(saved);
        } else {
            MeetupBean meetup = new MeetupBean();
            meetup.setEventParticipantId(myId);
            meetup.setExchangeRequestId(exchangeRequestId);
            meetup.setMeetupResult(value);
            ok = dao.insertMeetup(meetup) > 0;
        }

        if (!ok) {
            throw new ServletException("交流結果の登録に失敗しました。");
        }

        // 4. トップ画面（イベントルーム）へ
        response.sendRedirect(request.getContextPath() + "/admin/topScreen.jsp");
    }

    // 交流申請の相手のID（自分が当事者でなければ 0）
    private long findPartnerId(long exchangeRequestId, long myId) {
        if (exchangeRequestId <= 0) {
            return 0;
        }
        ExchangeRequestBean exchange =
                new ExchangeRequestDAO().findByExchangeRequestId(exchangeRequestId);

        return MeetupAction.getPartnerId(exchange, myId);
    }

    // 相手のニックネームを画面に渡す
    private void setPartner(HttpServletRequest request,
                            long partnerId, long exchangeRequestId)
            throws ServletException {
        try {
            ProfileBean profile = MeetupAction.findPartnerProfile(partnerId);

            request.setAttribute("partnerName",
                    profile != null ? profile.getNickname() : "相手");
            request.setAttribute("exchangeRequestId", exchangeRequestId);

        } catch (SQLException e) {
            throw new ServletException("相手の情報の取得に失敗しました。", e);
        }
    }

    // この交流申請で、自分がもう登録した結果（無ければ null）
    private MeetupBean findMyResult(long myId, long exchangeRequestId) {
        for (MeetupBean meetup : new MeetupDAO().findByEventParticipantId(myId)) {
            if (meetup.getExchangeRequestId() == exchangeRequestId) {
                return meetup;
            }
        }
        return null;
    }
}
