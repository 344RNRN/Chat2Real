package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Bean.ParticipantLoginBean;

/**
 * 参加者ログインの DAO
 *
 * 使うテーブル：t003_events / t006_events_participant / t007_profiles
 */
public class ParticipantLoginDAO {

    /** 何人当てはまったかの結果 */
    public enum Result { FOUND, NOT_FOUND, DUPLICATE }

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    /** イベントを1件（無ければ null） */
    public ParticipantLoginBean findEvent(long eventId) throws SQLException {
        String sql = "SELECT event_id, event_name, event_datetime, venue FROM public.t003_events WHERE event_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? toBean(rs) : null;
            }
        }
    }

    /** いちばん最近作られたイベント（URL に eventId が無いとき用。無ければ null） */
    public ParticipantLoginBean findLatestEvent() throws SQLException {
        String sql = "SELECT event_id, event_name, event_datetime, venue FROM public.t003_events "
                   + "ORDER BY created_at DESC, event_id DESC LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? toBean(rs) : null;
        }
    }

    /**
     * ニックネームと本名が両方一致する参加者を探す。
     *   1. 画面のイベント（bean.eventId）を先に探す
     *   2. そこにいなければ、ほかのイベントを新しい順に探す（ちがうイベントで登録していた場合）
     * 見つかったイベントで1人だけなら、bean に参加者とイベントを入れて FOUND。
     * 本名は空白（半角・全角）を取って比べる。ニックネームは前後の空白を取って比べる。
     */
    public Result login(ParticipantLoginBean bean) throws SQLException {
        String sql = "SELECT ep.event_participant_id, ep.participant_id, "
                   + "       e.event_id, e.event_name, e.event_datetime, e.venue "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t007_profiles p ON p.profile_id = ep.profile_id "
                   + "JOIN public.t003_events e ON e.event_id = ep.event_id "
                   + "WHERE btrim(p.nickname) = ? "
                   + "  AND regexp_replace(p.real_name, '[[:space:]　]+', '', 'g') = ? "
                   + "ORDER BY (e.event_id = ?) DESC, e.created_at DESC, e.event_id DESC, "
                   + "         ep.event_participant_id "
                   + "LIMIT 2";

        long requestedEventId = bean.getEventId();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bean.getNickname().trim());
            ps.setString(2, ParticipantLoginBean.normalizeName(bean.getRealName()));
            ps.setLong(3, requestedEventId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[ParticipantLogin] 一致する参加者がいません（全イベントを検索）");
                    return Result.NOT_FOUND;
                }

                long epId = rs.getLong("event_participant_id");
                long pId = rs.getLong("participant_id");
                long eventId = rs.getLong("event_id");
                String eventName = rs.getString("event_name");
                java.sql.Timestamp eventDatetime = rs.getTimestamp("event_datetime");
                String venue = rs.getString("venue");

                // 同じイベントにもう1人いたら、どちらか分からない
                if (rs.next() && rs.getLong("event_id") == eventId) {
                    System.out.println("[ParticipantLogin] event_id = " + eventId + " に同じ名前の参加者が2人以上います");
                    return Result.DUPLICATE;
                }

                if (eventId != requestedEventId) {
                    System.out.println("[ParticipantLogin] event_id = " + requestedEventId
                            + " にはいないため、登録していた event_id = " + eventId + " でログインします");
                }

                bean.setEventParticipantId(epId);
                bean.setParticipantId(pId);
                bean.setEventId(eventId);
                bean.setEventName(eventName);
                bean.setEventDatetime(eventDatetime);
                bean.setVenue(venue);
                return Result.FOUND;
            }
        }
    }

    private ParticipantLoginBean toBean(ResultSet rs) throws SQLException {
        ParticipantLoginBean b = new ParticipantLoginBean();
        b.setEventId(rs.getLong("event_id"));
        b.setEventName(rs.getString("event_name"));
        b.setEventDatetime(rs.getTimestamp("event_datetime"));
        b.setVenue(rs.getString("venue"));
        return b;
    }
}