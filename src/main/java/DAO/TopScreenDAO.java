package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import Bean.TopScreenBean;

/**
 * イベントルーム（トップ画面）の DAO
 *   t006_events_participant / t003_events / t007_profiles / t013_notifications
 * ※ 表示名の切り替えは t006 の display_name 列を使う（add_display_name.sql を先に実行）
 */
public class TopScreenDAO {

    public static final String NICKNAME = "nickname";
    public static final String REAL_NAME = "realName";

    // =========================================================
    // 自分の参加登録（event_participant_id）からトップ画面の内容を作る。無ければ null
    // =========================================================
    public TopScreenBean find(long eventParticipantId) throws SQLException {
        String sql = "SELECT ep.event_participant_id, ep.event_id, ep.exchange_status, ep.display_name, "
                   + "       e.event_name, e.event_datetime, e.end_datetime, e.venue, "
                   + "       p.nickname, p.real_name, "
                   + "       (SELECT COUNT(*) FROM public.t006_events_participant x "
                   + "         WHERE x.event_id = ep.event_id) AS participant_count, "
                   + "       (SELECT COUNT(*) FROM public.t006_events_participant x "
                   + "         WHERE x.event_id = ep.event_id AND x.exchange_status = '交流したい') AS want_count "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t003_events e ON e.event_id = ep.event_id "
                   + "LEFT JOIN public.t007_profiles p ON p.profile_id = ep.profile_id "
                   + "WHERE ep.event_participant_id = ?";

        try (Connection con = DBManager.getConnection()) {
            TopScreenBean b;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, eventParticipantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    b = new TopScreenBean();
                    b.setEventParticipantId(rs.getLong("event_participant_id"));
                    b.setEventId(rs.getLong("event_id"));
                    b.setExchangeStatus(rs.getString("exchange_status"));
                    b.setDisplayName(REAL_NAME.equals(rs.getString("display_name")) ? REAL_NAME : NICKNAME);
                    b.setEventName(rs.getString("event_name"));
                    b.setEventDate(dateText(rs.getTimestamp("event_datetime"), rs.getTimestamp("end_datetime")));
                    b.setVenue(rs.getString("venue"));
                    b.setNickname(rs.getString("nickname"));
                    b.setRealName(rs.getString("real_name"));
                    b.setParticipantCount(rs.getInt("participant_count"));
                    b.setWantCount(rs.getInt("want_count"));
                }
            }

            // 最新のお知らせ（運営者の「お知らせ配信」）
            String notice = "SELECT title, notification_text FROM public.t013_notifications "
                          + "WHERE event_id = ? ORDER BY notification_id DESC LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(notice)) {
                ps.setLong(1, b.getEventId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        b.setNoticeTitle(rs.getString("title"));
                        b.setNoticeText(rs.getString("notification_text"));
                    }
                }
            }
            return b;
        }
    }

    // =========================================================
    // 表示名（nickname / realName）を変える。更新できたら true
    // =========================================================
    public boolean updateDisplayName(long eventParticipantId, String displayName) throws SQLException {
        String value = REAL_NAME.equals(displayName) ? REAL_NAME : NICKNAME;
        String sql = "UPDATE public.t006_events_participant SET display_name = ? WHERE event_participant_id = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setLong(2, eventParticipantId);
            return ps.executeUpdate() == 1;
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

    /** 例：2026年11月9日（月）13:00〜17:00（終了日時が無ければ開始だけ） */
    private static String dateText(Timestamp start, Timestamp end) {
        if (start == null) {
            return null;
        }
        LocalDateTime s = start.toLocalDateTime();
        String week = s.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);
        String text = s.format(DateTimeFormatter.ofPattern("yyyy年M月d日")) + "（" + week + "）"
                    + s.format(DateTimeFormatter.ofPattern("HH:mm"));
        if (end != null) {
            LocalDateTime e = end.toLocalDateTime();
            text += "〜" + (e.toLocalDate().equals(s.toLocalDate())
                    ? e.format(DateTimeFormatter.ofPattern("HH:mm"))
                    : e.format(DateTimeFormatter.ofPattern("M/d HH:mm")));
        }
        return text;
    }
}
