package participant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import Bean.LoungeBean;
import DAO.LoungeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 資料・全体チャット画面（「今は交流したくない」を押したときの画面）
 *   GET  /participant/Lounge.action?tab=docs|chat   … 画面表示（スマホで最初に開くタブ）
 *   GET  /participant/Lounge.action?part=messages   … 自動更新用（メッセージ部分の HTML だけ）
 *   POST /participant/Lounge.action                 … 全体チャットに送信 → 画面に戻る
 */
@WebServlet("/participant/Lounge.action")
public class LoungeAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/lounge.jsp";
    private static final String REGISTER = "/participants/accountRegister.jsp";
    private static final int MAX_LENGTH = 500;

    /** 【開発用】セッションが空なら最新の参加登録で開く。※本番では false */
    private static final boolean DEV_FALLBACK = true;

    private final transient LoungeDAO dao = new LoungeDAO();

    // ---------------- 表示 ----------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            LoungeBean me = findMe(request);
            if (me == null) {
                response.sendRedirect(request.getContextPath() + REGISTER);
                return;
            }

            long chatId = dao.findOrCreateGroupChat(me.getEventId());
            me.setGroupChatId(chatId);
            me.setMessages(dao.findMessages(chatId, me.getEventId(), me.getParticipantId()));

            // 自動更新：メッセージ部分だけ返す
            if ("messages".equals(request.getParameter("part"))) {
                writeMessages(response, me.getMessages());
                return;
            }

            me.setItems(dao.findItems(me.getEventId()));

            String tab = "chat".equals(request.getParameter("tab")) ? "chat" : "docs";
            request.setAttribute("lounge", me);
            request.setAttribute("tab", tab);

            HttpSession s = request.getSession();             // 1回だけ出すメッセージ
            request.setAttribute("flash", s.getAttribute("loungeFlash"));
            s.removeAttribute("loungeFlash");

        } catch (SQLException e) {
            throw new ServletException("資料・全体チャットを読み込めませんでした", e);
        }

        request.getRequestDispatcher(JSP).forward(request, response);
    }

    // ---------------- 送信 ----------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String back = request.getContextPath() + "/participant/Lounge.action?tab=chat";

        try {
            LoungeBean me = findMe(request);
            if (me == null) {
                response.sendRedirect(request.getContextPath() + REGISTER);
                return;
            }

            String text = request.getParameter("message");
            text = (text == null) ? "" : text.trim();

            if (text.isEmpty() || text.length() > MAX_LENGTH) {
                request.getSession().setAttribute("loungeFlash",
                        "メッセージは1〜" + MAX_LENGTH + "文字で入力してください");
            } else {
                long chatId = dao.findOrCreateGroupChat(me.getEventId());
                dao.insertMessage(chatId, me.getParticipantId(), text);
            }
        } catch (SQLException e) {
            throw new ServletException("メッセージを送信できませんでした", e);
        }

        response.sendRedirect(back);       // 再読み込みで二重送信しないよう redirect
    }

    // ---------------- 共通 ----------------

    /** セッションの自分（event_participant_id）から、イベント・参加者IDなどを取り出す */
    private LoungeBean findMe(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession();

        long myId = 0L;
        Object v = session.getAttribute("eventParticipantId");
        if (v instanceof Number) {
            myId = ((Number) v).longValue();
        }
        if (myId <= 0 && DEV_FALLBACK) {
            myId = dao.findLatestEventParticipantId();
        }
        if (myId <= 0) {
            return null;
        }

        LoungeBean me = dao.findMe(myId);
        if (me != null) {
            // ほかの画面（全体チャット・下メニューなど）と同じキーでも入れておく
            session.setAttribute("eventParticipantId", me.getEventParticipantId());
            session.setAttribute("eventId", me.getEventId());
            session.setAttribute("event_id", me.getEventId());
            session.setAttribute("participantId", me.getParticipantId());
            session.setAttribute("participant_id", me.getParticipantId());
            session.setAttribute("exchangeStatus", me.getExchangeStatus());
        }
        return me;
    }

    /** 自動更新用：lounge.jsp の #messageList の中身と同じ形の HTML */
    private void writeMessages(HttpServletResponse response, List<LoungeBean.Message> messages)
            throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        StringBuilder html = new StringBuilder();
        if (messages.isEmpty()) {
            html.append("<p class=\"chat-empty\">まだメッセージはありません。最初のひとことを送ってみましょう！</p>");
        }
        for (LoungeBean.Message m : messages) {
            html.append("<div class=\"msg").append(m.isMine() ? " mine" : "").append("\">");
            if (!m.isMine()) {
                html.append("<span class=\"msg-name\">").append(escape(m.getNickname())).append("</span>");
            }
            html.append("<p class=\"msg-text\">").append(escape(m.getText())).append("</p>")
                .append("<span class=\"msg-time\">").append(escape(m.getTimeText())).append("</span>")
                .append("</div>\n");
        }
        response.getWriter().write(html.toString());
    }

    static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
