package participant;

import java.io.IOException;
import java.sql.SQLException;

import DAO.EventCloseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 「イベントは終了しました」画面
 *   GET /participant/EventClosed.action → /participants/eventClosed.jsp
 * 終了後に参加者の画面を開くと、EventEndedFilter がここへ移動させる
 */
@WebServlet("/participant/EventClosed.action")
public class EventClosedAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        long myId = 0L;
        if (session != null && session.getAttribute("eventParticipantId") instanceof Number) {
            myId = ((Number) session.getAttribute("eventParticipantId")).longValue();
        }

        try {
            Object[] status = (myId > 0) ? new EventCloseDAO().findStatus(myId) : null;
            if (status == null || !(Boolean) status[0]) {
                // まだ終了していない（or 参加登録が無い）→ イベントルームへ
                response.sendRedirect(request.getContextPath() + "/participant/Top.action");
                return;
            }
            request.setAttribute("eventName", status[2]);
        } catch (SQLException e) {
            throw new ServletException("イベントの状態を確認できませんでした", e);
        }

        request.getRequestDispatcher("/participants/eventClosed.jsp").forward(request, response);
    }
}
