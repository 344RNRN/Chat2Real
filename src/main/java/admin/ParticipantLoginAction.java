package admin;

import java.io.IOException;
import java.sql.SQLException;

import Bean.ParticipantLoginBean;
import DAO.ParticipantLoginDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 参加者ログイン（すでに参加登録した人が自分のアカウントに戻る）
 *
 *   GET  /participant/Login.action?eventId=34 … ログイン画面（eventId が無ければ最新のイベント）
 *   POST /participant/Login.action            … ニックネーム＋本名が一致したらログインしてプロフィールへ
 *
 * 参加者にはパスワードが無いため、参加登録のときのニックネームと本名の両方で本人を確認する。
 * 名前を当てずっぽうで試されないよう、5回まちがえたら10分待ってもらう。
 */
@WebServlet("/participant/Login.action")
public class ParticipantLoginAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/participantLogin.jsp";
    private static final String AFTER_LOGIN = "/participant/AccountProfile.action";

    /** まちがえてよい回数と、超えたときに待つ時間 */
    private static final int MAX_FAILS = 5;
    private static final long LOCK_MILLIS = 10 * 60 * 1000L;

    // =========================================================
    // GET：ログイン画面
    // =========================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ParticipantLoginBean bean = loadEvent(request);
        forward(request, response, bean, bean == null ? "ログインできるイベントがありません" : null);
    }

    // =========================================================
    // POST：ログイン
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        ParticipantLoginBean bean = loadEvent(request);
        if (bean == null) {
            forward(request, response, null, "ログインできるイベントがありません");
            return;
        }

        bean.setNickname(trim(request.getParameter("nickname")));
        bean.setRealName(trim(request.getParameter("realName")));

        // ---------- まちがいが多すぎるときは待ってもらう ----------
        HttpSession session = request.getSession();
        if (isLocked(session)) {
            forward(request, response, bean,
                    "入力の失敗が続いたため、しばらくログインできません。10分ほど待ってからもう一度お試しください");
            return;
        }

        // ---------- 入力チェック ----------
        if (bean.getNickname().isEmpty() || bean.getRealName().isEmpty()) {
            forward(request, response, bean, "ニックネームと本名を入力してください");
            return;
        }

        // ---------- 照合 ----------
        ParticipantLoginDAO.Result result;
        try {
            result = new ParticipantLoginDAO().login(bean);
        } catch (SQLException e) {
            throw new ServletException("ログインできませんでした。", e);
        }

        if (result == ParticipantLoginDAO.Result.NOT_FOUND) {
            countFail(session);
            // どちらがちがうかは言わない（名前を当てずっぽうで試されないように）
            forward(request, response, bean,
                    "ニックネームと本名が一致する参加者が見つかりません。参加登録のときと同じように入力してください");
            return;
        }
        if (result == ParticipantLoginDAO.Result.DUPLICATE) {
            forward(request, response, bean,
                    "同じニックネームと本名の参加者が複数いるため、ログインできません。運営スタッフにお声がけください");
            return;
        }

        // ---------- ログイン：参加登録（AccountRegisterAction）と同じ名前でセッションに入れる ----------
        request.changeSessionId();   // セッション固定攻撃の対策
        session.removeAttribute("loginFails");
        session.removeAttribute("loginLockedUntil");
        session.setAttribute("eventParticipantId", bean.getEventParticipantId());
        session.setAttribute("participantId", bean.getParticipantId());
        session.setAttribute("eventId", bean.getEventId());
        session.setAttribute("participant_id", bean.getParticipantId());   // 全体チャット用
        session.setAttribute("event_id", bean.getEventId());

        System.out.println("[ParticipantLogin] ログインしました：event_participant_id = " + bean.getEventParticipantId());
        response.sendRedirect(request.getContextPath() + AFTER_LOGIN);
    }

    // =========================================================
    // 共通
    // =========================================================

    /** ?eventId=… → 無ければ最新のイベント */
    private ParticipantLoginBean loadEvent(HttpServletRequest request) throws ServletException {
        ParticipantLoginDAO dao = new ParticipantLoginDAO();
        try {
            String param = trim(request.getParameter("eventId"));
            if (!param.isEmpty()) {
                try {
                    ParticipantLoginBean b = dao.findEvent(Long.parseLong(param));
                    if (b != null) return b;
                } catch (NumberFormatException ignore) {
                    // 数字でなければ最新のイベントにする
                }
            }
            return dao.findLatestEvent();
        } catch (SQLException e) {
            throw new ServletException("イベントを読み込めませんでした。", e);
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response,
                         ParticipantLoginBean bean, String error)
            throws ServletException, IOException {
        request.setAttribute("login", bean);
        request.setAttribute("loginLoaded", Boolean.TRUE);
        if (error != null) {
            request.setAttribute("errorMessage", error);
        }
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    private static boolean isLocked(HttpSession session) {
        Object until = session.getAttribute("loginLockedUntil");
        if (until instanceof Long && System.currentTimeMillis() < (Long) until) {
            return true;
        }
        if (until != null) {   // 待ち時間が過ぎたらやり直せる
            session.removeAttribute("loginLockedUntil");
            session.removeAttribute("loginFails");
        }
        return false;
    }

    private static void countFail(HttpSession session) {
        Object v = session.getAttribute("loginFails");
        int fails = (v instanceof Integer) ? (Integer) v + 1 : 1;
        session.setAttribute("loginFails", fails);
        if (fails >= MAX_FAILS) {
            session.setAttribute("loginLockedUntil", System.currentTimeMillis() + LOCK_MILLIS);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
