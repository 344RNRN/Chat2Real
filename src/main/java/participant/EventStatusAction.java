package participant;

import java.io.IOException;
import java.sql.SQLException;

import DAO.EventCloseDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 参加者画面の「終了お知らせ」（common/event-alert.jsp）が30秒ごとに聞きにくる
 *   GET /participant/EventStatus.action → {"ended":false,"minutesLeft":8}
 */
@WebServlet("/participant/EventStatus.action")
public class EventStatusAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        long myId = 0L;
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("eventParticipantId") instanceof Number) {
            myId = ((Number) session.getAttribute("eventParticipantId")).longValue();
        }

        boolean ended = false;
        long minutesLeft = -1;
        if (myId > 0) {
            try {
                Object[] status = new EventCloseDAO().findStatus(myId);
                if (status != null) {
                    ended = (Boolean) status[0];
                    minutesLeft = (Long) status[1];
                }
            } catch (SQLException e) {
                // わからないときは「終了していない」として返す
            }
        }
        response.getWriter().write("{\"ended\":" + ended + ",\"minutesLeft\":" + minutesLeft + "}");
    }
}
