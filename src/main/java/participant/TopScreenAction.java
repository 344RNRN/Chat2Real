package participant;

import java.io.IOException;
import java.sql.SQLException;

import Bean.TopScreenBean;
import DAO.TopScreenDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * イベントルーム（トップ画面）を表示する
 *   GET /participant/Top.action → /admin/topScreen.jsp
 * topScreen.jsp を直接開いたときも、JSP からここへ移動してくる
 */
@WebServlet({"/participant/Top.action", "/participant/TopScreen.action"})
public class TopScreenAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/admin/topScreen.jsp";
    private static final String REGISTER = "/participants/accountRegister.jsp";

    /** 【開発用】セッションが空なら最新の参加登録で開く。※本番では false */
    private static final boolean DEV_FALLBACK = true;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        TopScreenDAO dao = new TopScreenDAO();
        HttpSession session = request.getSession();

        try {
            long myId = myEventParticipantId(session);
            if (myId <= 0 && DEV_FALLBACK) {
                myId = dao.findLatestEventParticipantId();
            }

            TopScreenBean top = (myId > 0) ? dao.find(myId) : null;
            if (top == null) {
                // 参加登録していない → 参加登録画面へ
                response.sendRedirect(request.getContextPath() + REGISTER);
                return;
            }

            // ほかの画面（参加者一覧・チャットなど）と同じキーでセッションに入れておく
            session.setAttribute("eventParticipantId", top.getEventParticipantId());
            session.setAttribute("eventId", top.getEventId());
            session.setAttribute("event_id", top.getEventId());

            // topScreen.jsp が受け取る名前で渡す
            request.setAttribute("top", top);
            request.setAttribute("eventName", top.getEventName());
            request.setAttribute("eventDate", top.getEventDate());
            request.setAttribute("venue", top.getVenue());
            request.setAttribute("participantCount", top.getParticipantCount());
            request.setAttribute("wantCount", top.getWantCount());
            request.setAttribute("noticeTitle", top.getNoticeTitle());
            request.setAttribute("noticeText", top.getNoticeText());
            request.setAttribute("exchangeStatus", top.getExchangeStatus());
            request.setAttribute("displayName", top.getDisplayName());

        } catch (SQLException e) {
            throw new ServletException("イベントルームの情報を読み込めませんでした", e);
        }

        request.getRequestDispatcher(JSP).forward(request, response);
    }

    /** セッションの自分の event_participant_id（無ければ 0） */
    static long myEventParticipantId(HttpSession session) {
        if (session == null) {
            return 0L;
        }
        Object v = session.getAttribute("eventParticipantId");
        return (v instanceof Number) ? ((Number) v).longValue() : 0L;
    }
}
