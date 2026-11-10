package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 個人チャットの未読（Instagram・Messenger のような新着通知用）
 *   未読 ＝ 自分が最後にそのチャットを見た時刻（t014_chat_participants.last_read_at）より後に、相手が送ったメッセージ
 * ※ add_chat_unread.sql を先に実行しておくこと
 */
public class ChatUnreadDAO {

    /** いちばん新しい未読メッセージ（通知に出す） */
    public static class Latest {
        public long messageId;
        public long partnerId;      // 相手の event_participant_id
        public String nickname;
        public String text;
    }

    /** 相手ごとの未読数（相手の event_participant_id → 件数） */
    public Map<Long, Integer> countByPartner(long myEventParticipantId) throws SQLException {
        String sql =
            "SELECT other.event_participant_id, COUNT(m.message_id) " +
            "FROM public.t014_chat_participants mine " +
            "JOIN public.t006_events_participant me ON me.event_participant_id = mine.event_participant_id " +
            "JOIN public.t010_chats c ON c.chat_id = mine.chat_id AND c.chat_type = '個人' " +
            "JOIN public.t014_chat_participants other " +
            "  ON other.chat_id = mine.chat_id AND other.event_participant_id <> mine.event_participant_id " +
            "JOIN public.t011_messages m ON m.chat_id = c.chat_id " +
            "  AND m.sender_participant_id <> me.participant_id " +
            "  AND (mine.last_read_at IS NULL OR m.sent_at > mine.last_read_at) " +
            "WHERE mine.event_participant_id = ? " +
            "GROUP BY other.event_participant_id";

        Map<Long, Integer> map = new LinkedHashMap<>();
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, myEventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.merge(rs.getLong(1), rs.getInt(2), Integer::sum);
                }
            }
        }
        return map;
    }

    /** いちばん新しい未読メッセージ。無ければ null */
    public Latest findLatest(long myEventParticipantId) throws SQLException {
        String sql =
            "SELECT m.message_id, m.message_text, other.event_participant_id, p.nickname " +
            "FROM public.t014_chat_participants mine " +
            "JOIN public.t006_events_participant me ON me.event_participant_id = mine.event_participant_id " +
            "JOIN public.t010_chats c ON c.chat_id = mine.chat_id AND c.chat_type = '個人' " +
            "JOIN public.t014_chat_participants other " +
            "  ON other.chat_id = mine.chat_id AND other.event_participant_id <> mine.event_participant_id " +
            "JOIN public.t006_events_participant oep ON oep.event_participant_id = other.event_participant_id " +
            "LEFT JOIN public.t007_profiles p ON p.profile_id = oep.profile_id " +
            "JOIN public.t011_messages m ON m.chat_id = c.chat_id " +
            "  AND m.sender_participant_id <> me.participant_id " +
            "  AND (mine.last_read_at IS NULL OR m.sent_at > mine.last_read_at) " +
            "WHERE mine.event_participant_id = ? " +
            "ORDER BY m.sent_at DESC, m.message_id DESC LIMIT 1";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, myEventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Latest l = new Latest();
                l.messageId = rs.getLong("message_id");
                l.text = rs.getString("message_text");
                l.partnerId = rs.getLong("event_participant_id");
                l.nickname = rs.getString("nickname");
                return l;
            }
        }
    }

    /** そのチャットを「既読」にする（チャット画面を開いている間、ChatAction が呼ぶ） */
    public void markRead(long chatId, long myEventParticipantId) throws SQLException {
        String sql = "UPDATE public.t014_chat_participants SET last_read_at = LOCALTIMESTAMP "
                   + "WHERE chat_id = ? AND event_participant_id = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setLong(2, myEventParticipantId);
            ps.executeUpdate();
        }
    }
}
