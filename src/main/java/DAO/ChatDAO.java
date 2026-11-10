package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
<<<<<<< HEAD
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
=======
import java.util.ArrayList;
import java.util.List;
>>>>>>> branch 'main' of https://github.com/344RNRN/Chat2Real.git

import Bean.ChatBean;
<<<<<<< HEAD
import Bean.ChatMessageBean;
import Bean.ChatPartnerBean;
=======
>>>>>>> branch 'main' of https://github.com/344RNRN/Chat2Real.git
import Bean.MessageBean;

/**
 * チャットの DAO
 *   個人チャット（ChatAction・chat.jsp）     … 1～7
 *   全体チャット（AllChatAction・groupChat.jsp）… 8～12（もとの ChatDAO のメソッド）
 *
 * 接続先は context.xml の jdbc/chat2real（ログインと同じ）
 */
public class ChatDAO {

    public static final String STATUS_ENDED = "終了";
    public static final String STATUS_ACTIVE = "利用中";

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private Connection getConnection() throws SQLException {
        try {
            DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/chat2real");
            return ds.getConnection();
        } catch (NamingException e) {
            throw new SQLException("context.xml の jdbc/chat2real が見つかりません", e);
        }
    }

    // =====================================
    // 1. Find participant information
    // =====================================

    public ChatPartnerBean findPartner(long eventParticipantId)
            throws SQLException {

        String sql =
            "SELECT ep.event_participant_id, ep.event_id, " +
            "ep.participant_id, p.nickname, p.real_name, " +
            "p.profile_image_url " +
            "FROM t006_events_participant ep " +
            "LEFT JOIN t007_profiles p ON ep.profile_id = p.profile_id " +
            "WHERE ep.event_participant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventParticipantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChatPartnerBean bean = new ChatPartnerBean();
                    bean.setEventParticipantId(rs.getLong("event_participant_id"));
                    bean.setEventId(rs.getLong("event_id"));
                    bean.setParticipantId(rs.getLong("participant_id"));
                    bean.setNickname(rs.getString("nickname"));
                    bean.setRealName(rs.getString("real_name"));
                    bean.setPhotoUrl(rs.getString("profile_image_url"));
                    return bean;
                }
            }
        }

        return null;
    }

    // =====================================
    // 2. Find or create personal chat
    // =====================================

    public ChatBean findOrCreatePersonalChat(
            long eventId,
            long myEventParticipantId,
            long partnerEventParticipantId)
            throws SQLException {

        String findSql =
            "SELECT c.chat_id, c.event_id, c.chat_type, c.chat_status " +
            "FROM t010_chats c " +
            "JOIN t014_chat_participants a ON c.chat_id = a.chat_id " +
            "JOIN t014_chat_participants b ON c.chat_id = b.chat_id " +
            "WHERE c.event_id = ? " +
            "AND c.chat_type = '個人' " +
            "AND a.event_participant_id = ? " +
            "AND b.event_participant_id = ? " +
            "ORDER BY c.chat_id DESC LIMIT 1";

        try (Connection con = getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(findSql)) {
                ps.setLong(1, eventId);
                ps.setLong(2, myEventParticipantId);
                ps.setLong(3, partnerEventParticipantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapChat(rs);
                    }
                }
            }

            con.setAutoCommit(false);

            try {
                long chatId;

                String insertChat =
                    "INSERT INTO t010_chats " +
                    "(event_id, chat_type, chat_status) " +
                    "VALUES (?, '個人', ?) RETURNING chat_id";

                try (PreparedStatement ps = con.prepareStatement(insertChat)) {
                    ps.setLong(1, eventId);
                    ps.setString(2, STATUS_ACTIVE);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        chatId = rs.getLong("chat_id");
                    }
                }

                String insertMember =
                    "INSERT INTO t014_chat_participants " +
                    "(chat_id, event_participant_id) VALUES (?, ?)";

                try (PreparedStatement ps = con.prepareStatement(insertMember)) {
                    ps.setLong(1, chatId);
                    ps.setLong(2, myEventParticipantId);
                    ps.executeUpdate();

                    ps.setLong(1, chatId);
                    ps.setLong(2, partnerEventParticipantId);
                    ps.executeUpdate();
                }

                con.commit();

                ChatBean bean = new ChatBean();
                bean.setChatId(chatId);
                bean.setEventId(eventId);
                bean.setChatType("個人");
                bean.setChatStatus(STATUS_ACTIVE);
                return bean;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // =====================================
    // 3. Find messages（個人チャット：自分のメッセージかどうか付き）
    // =====================================

    public List<ChatMessageBean> findMessages(long chatId, long myParticipantId)
            throws SQLException {

        List<ChatMessageBean> list = new ArrayList<>();

        String sql =
            "SELECT message_id, sender_participant_id, " +
            "message_text, sent_at " +
            "FROM t011_messages " +
            "WHERE chat_id = ? " +
            "ORDER BY sent_at ASC, message_id ASC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessageBean bean = new ChatMessageBean();
                    bean.setMessageId(rs.getLong("message_id"));
                    bean.setParticipantId(rs.getLong("sender_participant_id"));
                    bean.setText(rs.getString("message_text"));

                    Timestamp timestamp = rs.getTimestamp("sent_at");
                    bean.setSentAt(timestamp.toLocalDateTime());
                    bean.setTime(timestamp.toLocalDateTime().format(TIME));

                    bean.setMine(rs.getLong("sender_participant_id") == myParticipantId);
                    list.add(bean);
                }
            }
        }

        return list;
    }

    // =====================================
    // 4. Insert message（個人チャット：利用中で、メンバーのときだけ）
    // =====================================

    public void insertMessage(long chatId, long participantId, String message)
            throws SQLException {

        String sql =
            "INSERT INTO t011_messages " +
            "(chat_id, sender_participant_id, message_text) " +
            "SELECT ?, ?, ? " +
            "WHERE EXISTS (" +
            "SELECT 1 FROM t010_chats " +
            "WHERE chat_id = ? AND chat_status = ?) " +
            "AND EXISTS (" +
            "SELECT 1 FROM t014_chat_participants cp " +
            "JOIN t006_events_participant ep " +
            "ON cp.event_participant_id = ep.event_participant_id " +
            "WHERE cp.chat_id = ? AND ep.participant_id = ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ps.setLong(2, participantId);
            ps.setString(3, message);
            ps.setLong(4, chatId);
            ps.setString(5, STATUS_ACTIVE);
            ps.setLong(6, chatId);
            ps.setLong(7, participantId);

            if (ps.executeUpdate() == 0) {
                throw new SQLException("チャットが終了しているか、参加者ではありません。");
            }
        }
    }

    // =====================================
    // 5. Count conversation round trips
    // =====================================

    public int countRoundTrips(long chatId, long myParticipantId)
            throws SQLException {

        String sql =
            "SELECT COUNT(*) / 2 AS round_trips FROM t011_messages WHERE chat_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("round_trips");
                }
            }
        }

        return 0;
    }

    // =====================================
    // 6. Update chat status（個人・全体どちらでも使う）
    // =====================================

    public boolean updateChatStatus(ChatBean chat) throws SQLException {

        String sql = "UPDATE t010_chats SET chat_status = ? WHERE chat_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, chat.getChatStatus());
            ps.setLong(2, chat.getChatId());

            return ps.executeUpdate() > 0;
        }
    }

    // =====================================
    // 7. Find common interests
    // =====================================

    public List<String> findCommonInterestNames(
            long eventId, long myParticipantId, long partnerParticipantId)
            throws SQLException {

        List<String> list = new ArrayList<>();

        String sql =
            "SELECT DISTINCT i.interest_name " +
            "FROM t009_participant_interests a " +
            "JOIN t009_participant_interests b " +
            "ON a.event_id = b.event_id " +
            "AND a.interest_id = b.interest_id " +
            "JOIN t008_interests i ON a.interest_id = i.interest_id " +
            "WHERE a.event_id = ? " +
            "AND a.participant_id = ? " +
            "AND b.participant_id = ? " +
            "ORDER BY i.interest_name";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, myParticipantId);
            ps.setLong(3, partnerParticipantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("interest_name"));
                }
            }
        }

        return list;
    }

    // =====================================
    // 8. チャットを新規登録（全体チャット用・もとの ChatDAO）
    // =====================================

    public long insertChat(ChatBean chat) throws SQLException {

        String sql =
            "INSERT INTO t010_chats (event_id, chat_type, chat_status) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, chat.getEventId());
            ps.setString(2, chat.getChatType());
            ps.setString(3, chat.getChatStatus());

            if (ps.executeUpdate() == 0) {
                return 0;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return 0;
    }

    // =====================================
    // 9. チャットIDからチャット情報を取得（もとの ChatDAO）
    // =====================================

    public ChatBean findByChatId(long chatId) throws SQLException {

        String sql =
            "SELECT chat_id, event_id, chat_type, chat_status FROM t010_chats WHERE chat_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapChat(rs);
                }
            }
        }

        return null;
    }

    // =====================================
    // 10. イベントIDから全体チャットIDを取得（全体チャット用・もとの ChatDAO）
    // =====================================

    public long findGroupChatId(long eventId) throws SQLException {

        String sql =
            "SELECT chat_id FROM t010_chats WHERE event_id = ? AND chat_type = 'group'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("chat_id");
                }
            }
        }

        return 0;
    }

    // =====================================
    // 11. メッセージ一覧を取得（全体チャット用・もとの ChatDAO）
    // =====================================

    public List<MessageBean> findMessages(long chatId) throws SQLException {

        List<MessageBean> list = new ArrayList<>();

        String sql =
            "SELECT message_id, chat_id, sender_participant_id, message_text, sent_at " +
            "FROM t011_messages WHERE chat_id = ? ORDER BY sent_at ASC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MessageBean message = new MessageBean();
                    message.setMessageId(rs.getLong("message_id"));
                    message.setChatId(rs.getLong("chat_id"));
                    message.setSenderParticipantId(rs.getLong("sender_participant_id"));
                    message.setMessageText(rs.getString("message_text"));
                    message.setSentAt(rs.getTimestamp("sent_at"));
                    list.add(message);
                }
            }
        }

        return list;
    }

    // =====================================
    // 12. メッセージを登録（全体チャット用・もとの ChatDAO）
    // =====================================

    public boolean insertMessage(MessageBean message) throws SQLException {

        String sql =
            "INSERT INTO t011_messages (chat_id, sender_participant_id, message_text) VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, message.getChatId());
            ps.setLong(2, message.getSenderParticipantId());
            ps.setString(3, message.getMessageText());

            return ps.executeUpdate() > 0;
        }
    }
<<<<<<< HEAD

    // =====================================
    // Helper
    // =====================================

    private ChatBean mapChat(ResultSet rs) throws SQLException {
        ChatBean bean = new ChatBean();
        bean.setChatId(rs.getLong("chat_id"));
        bean.setEventId(rs.getLong("event_id"));
        bean.setChatType(rs.getString("chat_type"));
        bean.setChatStatus(rs.getString("chat_status"));
        return bean;
    }
    
    /** 自分が最後にやりとりした個人チャットの相手（event_participant_id）。無ければ 0 */
    public long findLatestPartnerId(long myEventParticipantId) throws SQLException {

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

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, myEventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
=======
    
 // イベントIDから全体チャットIDを取得
    public long findGroupChatId(long eventId) throws SQLException {

        String sql =
                "SELECT chat_id FROM t010_chats " +
                "WHERE event_id = ? AND chat_type = 'group'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("chat_id");
                }
            }
        }

        return 0;
    }
 // メッセージ一覧を取得
    public List<MessageBean> findMessages(long chatId)
            throws SQLException {

        List<MessageBean> list = new ArrayList<>();

        String sql =
                "SELECT message_id, chat_id, sender_participant_id, " +
                "message_text, sent_at " +
                "FROM t011_messages " +
                "WHERE chat_id = ? " +
                "ORDER BY sent_at ASC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    MessageBean message = new MessageBean();

                    message.setMessageId(rs.getLong("message_id"));
                    message.setChatId(rs.getLong("chat_id"));
                    message.setSenderParticipantId(
                            rs.getLong("sender_participant_id"));
                    message.setMessageText(
                            rs.getString("message_text"));
                    message.setSentAt(
                            rs.getTimestamp("sent_at"));

                    list.add(message);
                }
            }
        }

        return list;
    }
 // メッセージを登録
    public boolean insertMessage(MessageBean message)
            throws SQLException {

        String sql =
                "INSERT INTO t011_messages " +
                "(chat_id, sender_participant_id, message_text) " +
                "VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, message.getChatId());
            ps.setLong(2, message.getSenderParticipantId());
            ps.setString(3, message.getMessageText());

            return ps.executeUpdate() > 0;
>>>>>>> branch 'main' of https://github.com/344RNRN/Chat2Real.git
        }
    }
}


