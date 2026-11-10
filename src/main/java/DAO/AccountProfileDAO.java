package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Bean.AccountProfileBean;

/**
 * 参加者のプロフィールを読む DAO（登録完了画面・プロフィール画面）
 *
 * 使うテーブル：
 *   t006_events_participant / t003_events / t007_profiles
 *   t009_participant_interests / t008_interests
 */
public class AccountProfileDAO {

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    /**
     * 【開発用】いちばん最近のイベントの、いちばん最近の参加者（プロフィールあり）。
     * 参加登録をしていない状態で画面を確認するときに使う。いなければ 0
     */
    public long findLatestEventParticipantId() throws SQLException {
        String sql = "SELECT ep.event_participant_id "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t003_events e ON e.event_id = ep.event_id "
                   + "WHERE ep.profile_id IS NOT NULL "
                   // チャットも確認できるように、「交流したい」の人を優先する
                   + "ORDER BY e.created_at DESC, e.event_id DESC, "
                   + "(ep.exchange_status = '交流したい') DESC, ep.event_participant_id DESC "
                   + "LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    /** event_participant_id で1人分。見つからなければ null */
    public AccountProfileBean findByEventParticipantId(long eventParticipantId) throws SQLException {
        String sql = "SELECT ep.event_participant_id, ep.participant_id, ep.exchange_status, "
                   + "       e.event_id, e.event_name, e.event_datetime, e.venue, "
                   + "       p.nickname, p.real_name, p.age_group, p.self_introduction, p.profile_image_url "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t003_events e ON e.event_id = ep.event_id "
                   + "LEFT JOIN public.t007_profiles p ON p.profile_id = ep.profile_id "
                   + "WHERE ep.event_participant_id = ?";

        try (Connection con = getConnection()) {
            AccountProfileBean b;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, eventParticipantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    b = new AccountProfileBean();
                    b.setEventParticipantId(rs.getLong("event_participant_id"));
                    b.setParticipantId(rs.getLong("participant_id"));
                    b.setExchangeStatus(rs.getString("exchange_status"));
                    b.setEventId(rs.getLong("event_id"));
                    b.setEventName(rs.getString("event_name"));
                    b.setEventDatetime(rs.getTimestamp("event_datetime"));
                    b.setVenue(rs.getString("venue"));
                    b.setNickname(rs.getString("nickname"));
                    b.setRealName(rs.getString("real_name"));
                    b.setAgeGroup(rs.getString("age_group"));
                    b.setSelfIntroduction(rs.getString("self_introduction"));
                    b.setProfileImageUrl(rs.getString("profile_image_url"));
                }
            }

            // 興味（このイベントで登録したもの）
            String interestSql = "SELECT i.interest_name "
                               + "FROM public.t009_participant_interests pi "
                               + "JOIN public.t008_interests i ON i.interest_id = pi.interest_id "
                               + "WHERE pi.event_id = ? AND pi.participant_id = ? "
                               + "ORDER BY i.interest_id";
            try (PreparedStatement ps = con.prepareStatement(interestSql)) {
                ps.setLong(1, b.getEventId());
                ps.setLong(2, b.getParticipantId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        b.getInterests().add(rs.getString("interest_name"));
                    }
                }
            }
            return b;
        }
    }
}