package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import Bean.LoungeBean;

/**
 * 資料・全体チャット画面（lounge.jsp）の DAO
 *   t006_events_participant / t003_events / t007_profiles
 *   t013_notifications / t014_event_materials
 *   t010_chats（chat_type = 'group'）/ t011_messages
 */
public class LoungeDAO {

    /** 全体チャットの種類（チームの AllChatAction と同じ値） */
    public static final String GROUP = "group";

    // =========================================================
    // 自分・イベントの情報。見つからなければ null
    // =========================================================
    public LoungeBean findMe(long eventParticipantId) throws SQLException {
        String sql = "SELECT ep.event_participant_id, ep.participant_id, ep.event_id, ep.exchange_status, "
                   + "       e.event_name, "
                   + "       (SELECT COUNT(*) FROM public.t006_events_participant x "
                   + "         WHERE x.event_id = ep.event_id) AS member_count "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t003_events e ON e.event_id = ep.event_id "
                   + "WHERE ep.event_participant_id = ?";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                LoungeBean b = new LoungeBean();
                b.setEventParticipantId(rs.getLong("event_participant_id"));
                b.setParticipantId(rs.getLong("participant_id"));
                b.setEventId(rs.getLong("event_id"));
                b.setExchangeStatus(rs.getString("exchange_status"));
                b.setEventName(rs.getString("event_name"));
                b.setMemberCount(rs.getInt("member_count"));
                return b;
            }
        }
    }

    // =========================================================
    // お知らせ・資料（お知らせ → 資料の新しい順）
    // =========================================================
    public List<LoungeBean.Item> findItems(long eventId) throws SQLException {
        List<LoungeBean.Item> list = new ArrayList<>();

        // お知らせ（添付ファイルがあれば一緒に。add_notice_attachment.sql で notification_id 列を追加）
        String noticeSql = "SELECT n.title, n.notification_text, n.created_at, "
                         + "       m.file_name, m.file_path, m.file_type "
                         + "FROM public.t013_notifications n "
                         + "LEFT JOIN LATERAL (SELECT file_name, file_path, file_type "
                         + "                   FROM public.t014_event_materials x "
                         + "                   WHERE x.notification_id = n.notification_id "
                         + "                   ORDER BY x.material_id LIMIT 1) m ON true "
                         + "WHERE n.event_id = ? ORDER BY n.notification_id DESC";
        // イベント作成のときに登録した資料（お知らせの添付は上で出すので除く）
        String fileSql = "SELECT file_name, file_path, file_type, created_at "
                       + "FROM public.t014_event_materials WHERE event_id = ? AND notification_id IS NULL "
                       + "ORDER BY created_at DESC, material_id DESC";

        try (Connection con = DBManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(noticeSql)) {
                ps.setLong(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LoungeBean.Item it = new LoungeBean.Item();
                        it.setNotice(true);
                        it.setTitle(rs.getString("title"));
                        it.setText(rs.getString("notification_text"));
                        it.setTimeText(timeText(rs.getTimestamp("created_at")));
                        it.setFilePath(rs.getString("file_path"));          // 添付（無ければ null）
                        it.setFileName(rs.getString("file_name"));
                        it.setFileType(rs.getString("file_type"));
                        list.add(it);
                    }
                }
            }

            {
                try (PreparedStatement ps = con.prepareStatement(fileSql)) {
                    ps.setLong(1, eventId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            LoungeBean.Item it = new LoungeBean.Item();
                            it.setTitle(rs.getString("file_name"));
                            it.setFileName(rs.getString("file_name"));
                            it.setFilePath(rs.getString("file_path"));
                            it.setFileType(rs.getString("file_type"));
                            it.setTimeText(timeText(rs.getTimestamp("created_at")));
                            list.add(it);
                        }
                    }
                }
            }
        }
        return list;
    }

    // =========================================================
    // イベントの全体チャット。無ければ作る
    // =========================================================
    public long findOrCreateGroupChat(long eventId) throws SQLException {
        String find = "SELECT chat_id FROM public.t010_chats WHERE event_id = ? AND chat_type = ? "
                    + "ORDER BY chat_id LIMIT 1";
        String create = "INSERT INTO public.t010_chats (event_id, chat_type) VALUES (?, ?) RETURNING chat_id";

        try (Connection con = DBManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(find)) {
                ps.setLong(1, eventId);
                ps.setString(2, GROUP);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(create)) {
                ps.setLong(1, eventId);
                ps.setString(2, GROUP);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }
    }

    // =========================================================
    // 全体チャットのメッセージ（古い順・最新 100 件）。送った人のニックネーム付き
    // =========================================================
    public List<LoungeBean.Message> findMessages(long chatId, long eventId, long myParticipantId)
            throws SQLException {
        String sql = "SELECT * FROM ("
                   + "  SELECT m.message_id, m.sender_participant_id, m.message_text, m.sent_at, "
                   + "         COALESCE(p.nickname, 'ゲスト') AS nickname "
                   + "  FROM public.t011_messages m "
                   + "  LEFT JOIN public.t006_events_participant ep "
                   + "         ON ep.participant_id = m.sender_participant_id AND ep.event_id = ? "
                   + "  LEFT JOIN public.t007_profiles p ON p.profile_id = ep.profile_id "
                   + "  WHERE m.chat_id = ? "
                   + "  ORDER BY m.sent_at DESC, m.message_id DESC LIMIT 100"
                   + ") t ORDER BY sent_at ASC, message_id ASC";

        List<LoungeBean.Message> list = new ArrayList<>();
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setLong(2, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoungeBean.Message m = new LoungeBean.Message();
                    m.setMessageId(rs.getLong("message_id"));
                    m.setNickname(rs.getString("nickname"));
                    m.setText(rs.getString("message_text"));
                    m.setTimeText(timeText(rs.getTimestamp("sent_at")));
                    m.setMine(rs.getLong("sender_participant_id") == myParticipantId);
                    list.add(m);
                }
            }
        }
        return list;
    }

    // =========================================================
    // 全体チャットに送信
    // =========================================================
    public void insertMessage(long chatId, long participantId, String text) throws SQLException {
        String sql = "INSERT INTO public.t011_messages (chat_id, sender_participant_id, message_text) "
                   + "VALUES (?, ?, ?)";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setLong(2, participantId);
            ps.setString(3, text);
            ps.executeUpdate();
        }
    }

    // =========================================================
    // 【開発用】セッションが空のとき：いちばん新しい参加登録。無ければ 0
    // =========================================================
    public long findLatestEventParticipantId() throws SQLException {
        String sql = "SELECT event_participant_id FROM public.t006_events_participant "
                   + "ORDER BY event_participant_id DESC LIMIT 1";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private static boolean tableExists(Connection con, String name) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT to_regclass(?) IS NOT NULL")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    /** 今日なら「14:12」、それ以外は「10/27 14:12」 */
    private static String timeText(Timestamp ts) {
        if (ts == null) {
            return "";
        }
        LocalDateTime t = ts.toLocalDateTime();
        return t.toLocalDate().equals(LocalDate.now())
                ? t.format(DateTimeFormatter.ofPattern("HH:mm"))
                : t.format(DateTimeFormatter.ofPattern("M/d HH:mm"));
    }
}
