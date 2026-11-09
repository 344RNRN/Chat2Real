package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * チャット終了画面（07_チャット終了）用の DAO
 *   t010_chats / t011_messages / t012_exchange_requests / t006_events_participant
 *
 * ※ t010_chats には「誰のチャットか」の列が無いので、
 *   交流申請（t012）かメッセージ（t011）に出てくる人を当事者とする
 */
public class ChatEndDAO {

    /** 個人チャットの種別（t010_chats.chat_type） */
    public static final String TYPE_PERSONAL = "個人";

    /** 終了したチャットの状態（t010_chats.chat_status。最初は「利用中」） */
    public static final String STATUS_ENDED = "終了";

    /** ep（t006）がチャット c（t010）の当事者である条件 */
    private static final String IS_PARTY =
              " (EXISTS (SELECT 1 FROM public.t012_exchange_requests r "
            + "          WHERE r.chat_id = c.chat_id "
            + "            AND (r.requester_event_participant_id = ep.event_participant_id "
            + "              OR r.target_event_participant_id = ep.event_participant_id)) "
            + "  OR EXISTS (SELECT 1 FROM public.t011_messages m "
            + "          WHERE m.chat_id = c.chat_id "
            + "            AND m.sender_participant_id = ep.participant_id)) ";

    /** 個人チャットの状態（利用中／終了）。個人チャットが無ければ null */
    public String findPersonalChatStatus(long chatId) throws SQLException {
        String sql = "SELECT chat_status FROM public.t010_chats "
                   + "WHERE chat_id = ? AND chat_type = ?";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ps.setString(2, TYPE_PERSONAL);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("chat_status") : null;
            }
        }
    }

    /** 自分がこのチャットの当事者か */
    public boolean isMember(long chatId, long eventParticipantId) throws SQLException {
        String sql = "SELECT 1 FROM public.t006_events_participant ep "
                   + "JOIN public.t010_chats c ON c.event_id = ep.event_id "
                   + "WHERE c.chat_id = ? AND ep.event_participant_id = ? AND" + IS_PARTY;

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ps.setLong(2, eventParticipantId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** チャットの相手のイベント参加者ID。わからなければ 0 */
    public long findPartnerId(long chatId, long myEventParticipantId) throws SQLException {
        String sql = "SELECT ep.event_participant_id FROM public.t006_events_participant ep "
                   + "JOIN public.t010_chats c ON c.event_id = ep.event_id "
                   + "WHERE c.chat_id = ? AND ep.event_participant_id <> ? AND" + IS_PARTY
                   + "ORDER BY ep.event_participant_id LIMIT 1";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);
            ps.setLong(2, myEventParticipantId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    /** チャットを「終了」にする（すでに終了していれば何もしない） */
    public void endChat(long chatId) throws SQLException {
        String sql = "UPDATE public.t010_chats SET chat_status = ? "
                   + "WHERE chat_id = ? AND chat_type = ? AND chat_status <> ?";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, STATUS_ENDED);
            ps.setLong(2, chatId);
            ps.setString(3, TYPE_PERSONAL);
            ps.setString(4, STATUS_ENDED);

            ps.executeUpdate();
        }
    }
}
