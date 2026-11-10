package participant;

import java.io.IOException;
import java.sql.SQLException;

import Bean.AccountProfileBean;
import DAO.AccountProfileDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 参加者アカウント登録完了画面
 *   accountRegister.jsp で「登録する」→ AccountRegisterAction → ここ
 *   → 「プロフィールを確認する」でプロフィール画面へ
 */
@WebServlet("/participant/AccountComplete.action")
public class AccountCompleteAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/accountComplete.jsp";
    private static final String REGISTER = "/participant/AccountRegister.action";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long myId = getMyEventParticipantId(request);

        AccountProfileBean profile;
        try {
            profile = new AccountProfileDAO().findByEventParticipantId(myId);
        } catch (SQLException e) {
            throw new ServletException("登録内容を読み込めませんでした。", e);
        }

        // 登録していない（セッションに参加者がいない）ときは登録画面へ
        if (profile == null) {
            response.sendRedirect(request.getContextPath() + REGISTER);
            return;
        }

        request.setAttribute("profile", profile);
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    /**
     * 開発用：参加登録をしていない（セッションが空の）ときに、DB の最新の参加者で画面を確認できるようにする。
     * ※本番では false にする（false なら参加登録画面に移動する）
     */
    private static final boolean DEV_FALLBACK = true;

    /** セッションから自分の event_participant_id を取り出す（無ければ 0） */
    private static long getMyEventParticipantId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object id = session.getAttribute("eventParticipantId");
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        if (!DEV_FALLBACK) {
            return 0L;
        }

        // 【開発用】最新のイベントの最新の参加者として表示する
        try {
            AccountProfileDAO dao = new AccountProfileDAO();
            long devId = dao.findLatestEventParticipantId();
            AccountProfileBean dev = (devId > 0) ? dao.findByEventParticipantId(devId) : null;
            if (dev == null) {
                return 0L;
            }
            session.setAttribute("eventParticipantId", dev.getEventParticipantId());
            session.setAttribute("participantId", dev.getParticipantId());
            session.setAttribute("eventId", dev.getEventId());
            session.setAttribute("participant_id", dev.getParticipantId());   // 全体チャット用
            session.setAttribute("event_id", dev.getEventId());
            System.out.println("[開発用] セッションに参加者が無いため、event_participant_id = "
                    + devId + "（" + dev.getNickname() + "）として表示します");
            return devId;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0L;
        }
    }
}