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
 * 参加者のプロフィール画面
 *
 *   /participant/AccountProfile.action          … 自分のプロフィール（チャットするボタンは無効）
 *   /participant/AccountProfile.action?id=12    … ほかの参加者のプロフィール（チャットできる）
 *                                                   id = 相手の event_participant_id
 *
 * ほかの参加者のときは、公開情報（ニックネーム・年代・一言・興味）だけを表示する。
 * 本名・写真の登録状況などの非公開情報は、本人のプロフィールにだけ表示する。
 */
@WebServlet("/participant/AccountProfile.action")
public class AccountProfileAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/accountProfile.jsp";
    private static final String REGISTER = "/participant/AccountRegister.action";

    /** 1対1チャット画面（ChatAction）の URL。?partnerId=相手の event_participant_id を付けて開く */
    public static final String PRIVATE_CHAT = "/chat";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long myId = getMyEventParticipantId(request);
        long targetId = parseLong(request.getParameter("id"));

        AccountProfileDAO dao = new AccountProfileDAO();
        AccountProfileBean me;
        AccountProfileBean target;

        try {
            // 自分（参加登録していなければ登録画面へ）
            me = dao.findByEventParticipantId(myId);
            if (me == null) {
                response.sendRedirect(request.getContextPath() + REGISTER);
                return;
            }

            // 表示する相手（指定が無い・自分を指定したときは自分）
            target = (targetId > 0 && targetId != myId) ? dao.findByEventParticipantId(targetId) : me;

        } catch (SQLException e) {
            throw new ServletException("プロフィールを読み込めませんでした。", e);
        }

        // 見つからない・別のイベントの参加者なら自分のプロフィールを出す
        if (target == null || target.getEventId() != me.getEventId()) {
            response.sendRedirect(request.getContextPath() + "/participant/AccountProfile.action");
            return;
        }

        boolean isMine = (target.getEventParticipantId() == me.getEventParticipantId());

        // チャットできるか（できないときは理由を画面に出す）
        String chatDisabledReason = null;
        if (isMine) {
            chatDisabledReason = "自分のプロフィールです。ほかの参加者のプロフィールからチャットできます。";
        } else if (!target.isWantToMeet()) {
            chatDisabledReason = "この参加者は、今は交流を希望していません。";
        } else if (!me.isWantToMeet()) {
            chatDisabledReason = "チャットするには、イベントルームで「交流したい」を選んでください。";
        }

        request.setAttribute("profile", target);
        request.setAttribute("isMine", isMine);
        request.setAttribute("canChat", chatDisabledReason == null);
        request.setAttribute("chatDisabledReason", chatDisabledReason);
        request.setAttribute("chatUrl", request.getContextPath() + PRIVATE_CHAT
                + "?partnerId=" + target.getEventParticipantId());
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

    private static long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}