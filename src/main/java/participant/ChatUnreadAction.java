package participant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import DAO.ChatUnreadDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 新着メッセージの確認（common/chat-notify.jsp が数秒ごとに聞きにくる）
 *   GET /participant/ChatUnread.action
 *   → {"total":3,"byPartner":{"12":2,"15":1},
 *      "latest":{"messageId":55,"partnerId":12,"name":"Hsu","text":"こんにちは"}}
 */
@WebServlet("/participant/ChatUnread.action")
public class ChatUnreadAction extends HttpServlet {

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

        StringBuilder json = new StringBuilder("{\"total\":");
        if (myId <= 0) {
            response.getWriter().write("{\"total\":0,\"byPartner\":{},\"latest\":null}");
            return;
        }

        try {
            ChatUnreadDAO dao = new ChatUnreadDAO();
            Map<Long, Integer> byPartner = dao.countByPartner(myId);

            int total = 0;
            StringBuilder map = new StringBuilder("{");
            for (Map.Entry<Long, Integer> e : byPartner.entrySet()) {
                if (map.length() > 1) map.append(',');
                map.append('"').append(e.getKey()).append("\":").append(e.getValue());
                total += e.getValue();
            }
            map.append('}');

            json.append(total).append(",\"byPartner\":").append(map).append(",\"latest\":");

            ChatUnreadDAO.Latest l = (total > 0) ? dao.findLatest(myId) : null;
            if (l == null) {
                json.append("null");
            } else {
                json.append("{\"messageId\":").append(l.messageId)
                    .append(",\"partnerId\":").append(l.partnerId)
                    .append(",\"name\":").append(str(l.nickname == null ? "参加者" : l.nickname))
                    .append(",\"text\":").append(str(l.text))
                    .append('}');
            }
            json.append('}');

        } catch (SQLException e) {
            // last_read_at 列が無い・DB に繋がらないときは「0件」として返す（画面は止めない）
            json = new StringBuilder("{\"total\":0,\"byPartner\":{},\"latest\":null}");
        }
        response.getWriter().write(json.toString());
    }

    /** JSON の文字列（" や改行などをエスケープ） */
    private static String str(String s) {
        if (s == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': break;
                case '\t': sb.append("\\t");  break;
                case '<':  sb.append("\\u003c"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
}
