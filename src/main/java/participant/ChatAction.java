package participant;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Bean.ChatBean;
import Bean.ChatConversationBean;
import Bean.ChatMessageBean;
import Bean.ChatPartnerBean;
import DAO.ChatDAO;
import DAO.ChatUnreadDAO;
import DAO.DBManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 06 個人チャット画面（chat.jsp）用のサーブレット。1本で4つのURLを受け持つ。
 *   GET  /chat?partnerId=相手のevent_participant_id … 画面表示（チャットがなければ作成）
 *   GET  /chat/messages?partnerId=…                 … 画面の自動更新用（メッセージ部分の HTML だけ返す）
 *   POST /chat/send … メッセージ保存 → /chat に戻る
 *   POST /chat/end  … チャットを「終了」にして チャット終了画面へ
 *
 * 参加者一覧・プロフィール画面の「チャットする」から /chat?partnerId=… で開く。
 */
@WebServlet({"/chat", "/chat/messages", "/chat/send", "/chat/end"})
public class ChatAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ★ 参加登録が終わったときに session へ入れておく「自分の event_participant_id」（Long）のキー名。違う名前ならここだけ直す
    private static final String SESSION_EVENT_PARTICIPANT_ID =
    	    "eventParticipantId";
    // ★ forward 先のJSP。置き場所が違うならここだけ直す
    private static final String VIEW_CHAT = "/participants/chat.jsp";
    // ★ チャット終了画面（チームの ChatEndAction）。?chatId=… を付けて移動する
    private static final String CHAT_END = "/participant/ChatEnd.action";
    /** やりとりがこの往復数になったら、画面下の通知バー（申請・終了）を出す */
    private static final int NOTICE_ROUND_TRIPS = 3;
    private static final int MAX_LENGTH = 200;

    private final transient ChatDAO dao = new ChatDAO();

    /** 1リクエスト分の「自分・相手・チャット」 */
    private static class Ctx {
        ChatBean chat;
        ChatPartnerBean me;
        ChatPartnerBean partner;
        boolean ended() { return ChatDAO.STATUS_ENDED.equals(chat.getChatStatus()); }
    }

    // ---------------- 表示 ----------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Ctx c = open(req, resp);
        if (c == null) {
            return;
        }

        // このチャットを見ている ＝ 既読（参加者一覧の通知バッジから消える）
        markRead(c);

        // 自動更新（3秒ごと）：メッセージ部分の HTML だけ返す
        if ("/chat/messages".equals(req.getServletPath())) {
            writeMessages(resp, c);
            return;
        }

        if (c.ended()) {                       // 相手が終了していたらチャット終了画面へ
            showEnded(req, resp, c);
            return;
        }

        try {
            long chatId = c.chat.getChatId();
            long myPid = c.me.getParticipantId();

            req.setAttribute("chatId", chatId);                           // 終了画面への移動に使う

            List<ChatMessageBean> messages = dao.findMessages(chatId, myPid);
            int roundTrips = dao.countRoundTrips(chatId, myPid);

            req.setAttribute("partner", c.partner);                       // ① 相手
            req.setAttribute("conversations", findConversations(c.me.getId())); // 左のチャット一覧（全員）
            req.setAttribute("messages", messages);                       // ③④ メッセージ
            req.setAttribute("startedLabel", label(messages.isEmpty() ? null : messages.get(0).getSentAt()));  // ②
            req.setAttribute("commonTags", dao.findCommonInterestNames(   // ⑤ 共通の興味
                    c.me.getEventId(), myPid, c.partner.getParticipantId()));
            req.setAttribute("roundTrips", roundTrips);
            req.setAttribute("showRequestNotice", roundTrips >= NOTICE_ROUND_TRIPS);   // ⑥〜⑧ 通知バー

            HttpSession s = req.getSession();                             // 1回だけ出すメッセージ
            req.setAttribute("flashMessage", s.getAttribute("chatFlash"));
            s.removeAttribute("chatFlash");
        } catch (SQLException e) {
            throw new ServletException("チャット画面のDB取得に失敗しました", e);
        }
        req.getRequestDispatcher(VIEW_CHAT).forward(req, resp);
    }

    // ---------------- 送信・終了 ----------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        Ctx c = open(req, resp);
        if (c == null) {
            return;
        }
        String back = req.getContextPath() + "/chat?partnerId=" + c.partner.getId();

        try {
            if ("/chat/end".equals(req.getServletPath())) {
                if (!c.ended()) {
                    c.chat.setChatStatus(ChatDAO.STATUS_ENDED);
                    dao.updateChatStatus(c.chat);
                }
                showEnded(req, resp, c);
                return;
            }

            // /chat/send
            if (c.ended()) {
                showEnded(req, resp, c);
                return;
            }
            String text = req.getParameter("message");
            text = (text == null) ? "" : text.trim();
            if (text.isEmpty() || text.length() > MAX_LENGTH) {
                req.getSession().setAttribute("chatFlash", "メッセージは1〜" + MAX_LENGTH + "文字で入力してください");
            } else {
                dao.insertMessage(c.chat.getChatId(), c.me.getParticipantId(), text);
                markRead(c);
            }
            resp.sendRedirect(back);           // 再読み込みで二重送信されないよう redirect
        } catch (SQLException e) {
            throw new ServletException("チャットの更新に失敗しました", e);
        }
    }

    // ---------------- 共通 ----------------

    /** チャット終了画面（チームの ChatEndAction）へ */
    private void showEnded(HttpServletRequest req, HttpServletResponse resp, Ctx c)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + CHAT_END + "?chatId=" + c.chat.getChatId());
    }

    /**
     * 自動更新用：メッセージ一覧の HTML（chat.jsp の #messageList の中身と同じ形）を返す。
     * チャットが終了していたら X-Chat-Ended ヘッダーを付ける（画面側で終了画面へ移動する）
     */
    private void writeMessages(HttpServletResponse resp, Ctx c) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        if (c.ended()) {
            resp.setHeader("X-Chat-Ended", "1");
            return;
        }

        List<ChatMessageBean> messages;
        try {
            messages = dao.findMessages(c.chat.getChatId(), c.me.getParticipantId());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        StringBuilder html = new StringBuilder();
        for (ChatMessageBean m : messages) {
            html.append("<div class=\"msg ").append(m.isMine() ? "me" : "other").append("\">")
                .append("<p>").append(escape(m.getText())).append("</p>")
                .append("<span class=\"meta\">").append(escape(m.getTime())).append("</span>")
                .append("</div>\n");
        }
        resp.getWriter().write(html.toString());
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    /** ログイン中の本人・相手・チャットを特定する。問題があればエラーを返して null。 */
    private Ctx open(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Long myEp = null;
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute(SESSION_EVENT_PARTICIPANT_ID) instanceof Number) {
            myEp = ((Number) s.getAttribute(SESSION_EVENT_PARTICIPANT_ID)).longValue();
        }
        if (myEp == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "参加登録が済んでいません。QRコードから参加登録してください");
            return null;
        }
        // 相手が指定されていない（参加者一覧の「チャット画面へ」・左メニューの「チャット」など）ときは、
        //   最後にやりとりした相手のチャットへ。まだだれともチャットしていなければ
        //   チャット画面で「チャットする相手を選んでください」を表示する
        String partnerParam = req.getParameter("partnerId");
        if ((partnerParam == null || partnerParam.trim().isEmpty())
                && "GET".equals(req.getMethod()) && "/chat".equals(req.getServletPath())) {

            long latest = findLatestPartnerId(myEp);
            if (latest > 0) {
                resp.sendRedirect(req.getContextPath() + "/chat?partnerId=" + latest);
            } else {
                try {
                    req.getRequestDispatcher(VIEW_CHAT).forward(req, resp);   // partner なし → chat.jsp が案内を出す
                } catch (ServletException e) {
                    throw new IOException(e);
                }
            }
            return null;
        }

        long partnerEp;
        try {
            partnerEp = Long.parseLong(partnerParam.trim());
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "partnerId が正しくありません");
            return null;
        }
        if (partnerEp == myEp) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "自分自身とはチャットできません");
            return null;
        }

        try {
            Ctx c = new Ctx();
            c.me = dao.findPartner(myEp);
            c.partner = dao.findPartner(partnerEp);
            if (c.me == null || c.partner == null || c.me.getEventId() != c.partner.getEventId()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "相手が見つかりません");
                return null;
            }
            c.chat = dao.findOrCreatePersonalChat(c.me.getEventId(), myEp, partnerEp);
            return c;
        } catch (SQLException e) {
            throw new IOException("DBエラー: " + e.getMessage(), e);
        }
    }

    /** そのチャットを既読にする（失敗しても画面は止めない。add_chat_unread.sql が未実行のときなど） */
    private void markRead(Ctx c) {
        try {
            new ChatUnreadDAO().markRead(c.chat.getChatId(), c.me.getId());
        } catch (SQLException e) {
            System.out.println("[既読] 更新できませんでした：" + e.getMessage());
        }
    }

    /**
     * 左の「チャット一覧」：自分がチャットしたことのある相手 全員（新しいメッセージ順）
     * ※ 同じ相手と複数のチャットがあるときは新しい方だけ
     * ※ t014_chat_participants が無いときは空（エラーにしない）
     */
    private List<ChatConversationBean> findConversations(long myEventParticipantId) {
        String sql =
            "SELECT other.event_participant_id, c.chat_id, c.chat_status, " +
            "       p.nickname, p.profile_image_url, lm.message_text, lm.sent_at, " +
            "       (SELECT COUNT(*) FROM t011_messages u " +
            "         WHERE u.chat_id = c.chat_id AND u.sender_participant_id <> me.participant_id " +
            "           AND (mine.last_read_at IS NULL OR u.sent_at > mine.last_read_at)) AS unread " +
            "FROM t014_chat_participants mine " +
            "JOIN t014_chat_participants other " +
            "  ON other.chat_id = mine.chat_id AND other.event_participant_id <> mine.event_participant_id " +
            "JOIN t010_chats c ON c.chat_id = mine.chat_id AND c.chat_type = '個人' " +
            "JOIN t006_events_participant me ON me.event_participant_id = mine.event_participant_id " +
            "JOIN t006_events_participant ep ON ep.event_participant_id = other.event_participant_id " +
            "LEFT JOIN t007_profiles p ON p.profile_id = ep.profile_id " +
            "LEFT JOIN LATERAL (SELECT message_text, sent_at FROM t011_messages m " +
            "                   WHERE m.chat_id = c.chat_id " +
            "                   ORDER BY m.sent_at DESC, m.message_id DESC LIMIT 1) lm ON true " +
            "WHERE mine.event_participant_id = ? " +
            "ORDER BY lm.sent_at DESC NULLS LAST, c.chat_id DESC";

        Map<Long, ChatConversationBean> byPartner = new LinkedHashMap<>();
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, myEventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long partnerId = rs.getLong("event_participant_id");
                    if (byPartner.containsKey(partnerId)) {
                        continue;                       // 同じ相手は新しいチャットだけ
                    }
                    ChatConversationBean row = new ChatConversationBean();
                    row.setPartnerId(partnerId);
                    row.setChatId(rs.getLong("chat_id"));
                    row.setEnded(ChatDAO.STATUS_ENDED.equals(rs.getString("chat_status")));
                    row.setNickname(rs.getString("nickname"));
                    row.setPhotoUrl(rs.getString("profile_image_url"));
                    row.setLastMessage(rs.getString("message_text"));
                    row.setUnreadCount(rs.getInt("unread"));
                    java.sql.Timestamp at = rs.getTimestamp("sent_at");
                    row.setTimeText(at == null ? "" : shortTime(at.toLocalDateTime()));
                    byPartner.put(partnerId, row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(byPartner.values());
    }

    /** 一覧の時刻：今日なら「14:12」、それ以外は「11/9」 */
    private static String shortTime(LocalDateTime t) {
        return t.toLocalDate().equals(LocalDate.now())
                ? t.format(DateTimeFormatter.ofPattern("HH:mm"))
                : t.format(DateTimeFormatter.ofPattern("M/d"));
    }

    /**
     * 自分が最後にやりとりした個人チャットの相手（event_participant_id）。無ければ 0
     * ※ ChatDAO にメソッドを足さなくていいように、ここで直接 DB を見る
     * ※ t014_chat_participants（add_chat_participants.sql）が無いときも 0（エラーにしない）
     */
    private long findLatestPartnerId(long myEventParticipantId) {
        String sql =
            "SELECT other.event_participant_id " +
            "FROM t014_chat_participants mine " +
            "JOIN t014_chat_participants other " +
            "  ON other.chat_id = mine.chat_id AND other.event_participant_id <> mine.event_participant_id " +
            "JOIN t010_chats c ON c.chat_id = mine.chat_id AND c.chat_type = '個人' " +
            "LEFT JOIN t011_messages m ON m.chat_id = c.chat_id " +
            "WHERE mine.event_participant_id = ? " +
            "GROUP BY other.event_participant_id, c.chat_id, c.chat_status " +
            "ORDER BY (c.chat_status = '利用中') DESC, MAX(m.sent_at) DESC NULLS LAST, c.chat_id DESC " +
            "LIMIT 1";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, myEventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /** 日時ラベル：今日なら「今日 14:12」、それ以外は「10/27 14:12」 */
    private static String label(LocalDateTime t) {
        if (t == null) {
            t = LocalDateTime.now();
        }
        String time = t.format(DateTimeFormatter.ofPattern("HH:mm"));
        return t.toLocalDate().equals(LocalDate.now())
                ? "今日 " + time
                : t.format(DateTimeFormatter.ofPattern("M/d")) + " " + time;
    }
}
