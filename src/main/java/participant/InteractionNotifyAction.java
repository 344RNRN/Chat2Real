package participant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DAO.InteractionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 交流申請のお知らせ（JSON）
 *   GET /interaction/notify
 *   → {"notices":[{"id":12,"partnerId":5,"partnerName":"hh1","type":"incoming"}, …]}
 * common/request-notify.jsp が 5秒ごとに聞いて、画面の上にお知らせを出す。
 */
@WebServlet("/interaction/notify")
public class InteractionNotifyAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession(false);
        Object v = (session == null) ? null : session.getAttribute("eventParticipantId");
        if (!(v instanceof Number)) {
            resp.getWriter().write("{\"notices\":[]}");
            return;
        }
        long myId = ((Number) v).longValue();

        List<InteractionDAO.Notice> list;
        try {
            list = new InteractionDAO().findNotices(myId);
        } catch (SQLException e) {
            throw new ServletException("交流申請のお知らせを読み込めませんでした。", e);
        }

        StringBuilder sb = new StringBuilder("{\"notices\":[");
        for (int i = 0; i < list.size(); i++) {
            InteractionDAO.Notice n = list.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"id\":").append(n.exchangeRequestId)
              .append(",\"partnerId\":").append(n.partnerId)
              .append(",\"partnerName\":\"").append(json(n.partnerName == null ? "相手" : n.partnerName))
              .append("\",\"type\":\"").append(n.type).append("\"}");
        }
        sb.append("]}");
        resp.getWriter().write(sb.toString());
    }

    private static String json(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '<':  sb.append("\\u003c"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}
