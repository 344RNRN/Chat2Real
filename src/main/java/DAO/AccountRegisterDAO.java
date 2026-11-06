package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import Bean.AccountRegisterBean;
import Bean.InterestBean;

/**
 * 参加者アカウント登録の DAO
 *
 * 使うテーブル：
 *   t003_events / t008_interests / t015_event_interests（表示用）
 *   t005_participant / t007_profiles / t006_events_participant / t009_participant_interests（登録）
 */
public class AccountRegisterDAO {

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    // =========================================================
    // 画面表示：参加するイベント
    // =========================================================

    /** イベントを1件（無ければ null） */
    public AccountRegisterBean findEvent(long eventId) throws SQLException {
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
    public AccountRegisterBean findLatestEvent() throws SQLException {
        String sql = "SELECT event_id, event_name, event_datetime, venue FROM public.t003_events "
                   + "ORDER BY created_at DESC, event_id DESC LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? toBean(rs) : null;
        }
    }

    /**
     * そのイベントの興味タグ（イベント作成画面でチェックしたもの）
     * イベントにタグが1つも無いときは、有効なタグを全部出す
     */
    public List<InterestBean> findInterests(long eventId) throws SQLException {
        String eventSql = "SELECT i.interest_id, i.interest_name, i.is_active "
                        + "FROM public.t015_event_interests ei "
                        + "JOIN public.t008_interests i ON i.interest_id = ei.interest_id "
                        + "WHERE ei.event_id = ? AND i.is_active = true ORDER BY i.interest_id";
        String allSql = "SELECT interest_id, interest_name, is_active FROM public.t008_interests "
                      + "WHERE is_active = true ORDER BY interest_id";

        try (Connection con = getConnection()) {
            List<InterestBean> list = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(eventSql)) {
                ps.setLong(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(toInterest(rs));
                }
            }
            if (!list.isEmpty()) {
                return list;
            }

            try (PreparedStatement ps = con.prepareStatement(allSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(toInterest(rs));
            }
            return list;
        }
    }

    // =========================================================
    // 登録：参加者・プロフィール・イベント参加・興味をまとめて登録
    // どれか1つでも失敗したら全部取り消す
    // =========================================================
    public void register(AccountRegisterBean b) throws SQLException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. 参加者
                long participantId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO public.t005_participant DEFAULT VALUES RETURNING participant_id");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    participantId = rs.getLong(1);
                }

                // 2. プロフィール
                long profileId;
                String profileSql = "INSERT INTO public.t007_profiles "
                                  + "(participant_id, nickname, real_name, age_group, self_introduction, profile_image_url) "
                                  + "VALUES (?, ?, ?, ?, ?, ?) RETURNING profile_id";
                try (PreparedStatement ps = con.prepareStatement(profileSql)) {
                    ps.setLong(1, participantId);
                    ps.setString(2, b.getNickname());
                    ps.setString(3, b.getRealName());
                    ps.setString(4, b.getAgeGroup());
                    ps.setString(5, b.getSelfIntroduction());
                    ps.setString(6, b.getProfileImageUrl());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        profileId = rs.getLong(1);
                    }
                }

                // 3. イベントに参加（exchange_status は DB の初期値「交流したい」）
                long eventParticipantId;
                String epSql = "INSERT INTO public.t006_events_participant (event_id, participant_id, profile_id) "
                             + "VALUES (?, ?, ?) RETURNING event_participant_id";
                try (PreparedStatement ps = con.prepareStatement(epSql)) {
                    ps.setLong(1, b.getEventId());
                    ps.setLong(2, participantId);
                    ps.setLong(3, profileId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        eventParticipantId = rs.getLong(1);
                    }
                }

                // 4. 興味（チェックしたタグ ＋「その他」に書いたもの。重複は1回だけ）
                Set<Long> ids = new LinkedHashSet<>(b.getInterestIds());
                for (String name : b.getOtherInterests()) {
                    ids.add(findOrCreateInterest(con, name));
                }
                String piSql = "INSERT INTO public.t009_participant_interests (event_id, participant_id, interest_id) "
                             + "VALUES (?, ?, ?) ON CONFLICT (event_id, participant_id, interest_id) DO NOTHING";
                try (PreparedStatement ps = con.prepareStatement(piSql)) {
                    for (Long id : ids) {
                        ps.setLong(1, b.getEventId());
                        ps.setLong(2, participantId);
                        ps.setLong(3, id);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();

                b.setParticipantId(participantId);
                b.setProfileId(profileId);
                b.setEventParticipantId(eventParticipantId);

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- 部品 ----------

    private long findOrCreateInterest(Connection con, String name) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT interest_id FROM public.t008_interests WHERE interest_name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO public.t008_interests (interest_name) VALUES (?) RETURNING interest_id")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private AccountRegisterBean toBean(ResultSet rs) throws SQLException {
        AccountRegisterBean b = new AccountRegisterBean();
        b.setEventId(rs.getLong("event_id"));
        b.setEventName(rs.getString("event_name"));
        b.setEventDatetime(rs.getTimestamp("event_datetime"));
        b.setVenue(rs.getString("venue"));
        return b;
    }

    private InterestBean toInterest(ResultSet rs) throws SQLException {
        InterestBean i = new InterestBean();
        i.setInterestId(rs.getLong("interest_id"));
        i.setInterestName(rs.getString("interest_name"));
        i.setActive(rs.getBoolean("is_active"));
        return i;
    }
}