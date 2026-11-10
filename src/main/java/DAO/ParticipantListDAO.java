package DAO;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Bean.InterestBean;
import Bean.ParticipantListBean;

/**
 * 参加者一覧画面の DAO
 *
 * 表示するのは「同じイベント」「交流したい」「自分以外」の参加者だけ。
 * 本名は検索にも表示にも使わない（運営者のみ閲覧できる情報のため）。
 *
 * 使うテーブル：
 *   t006_events_participant / t007_profiles / t009_participant_interests / t008_interests
 */
public class ParticipantListDAO {

    /** 交流したいことを表す exchange_status の値（DB の初期値と同じ） */
    public static final String WANT = "交流したい";

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    /** 交流したい参加者の人数（自分以外・絞り込み前） */
    public int countWant(long eventId, long myEventParticipantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM public.t006_events_participant "
                   + "WHERE event_id = ? AND event_participant_id <> ? AND exchange_status = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setLong(2, myEventParticipantId);
            ps.setString(3, WANT);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * 参加者を検索して bean に入れる（participants・hitCount・otherCount）
     *
     * @param keyword    ニックネームの一部（空なら絞り込まない）
     * @param ageGroup   年代（空なら絞り込まない）
     * @param interestId 興味（0 なら絞り込まない）
     * @param limit      最大何人表示するか（0 以下なら全員）
     */
    public void search(ParticipantListBean bean, String keyword, String ageGroup,
                       long interestId, int limit) throws SQLException {

        StringBuilder where = new StringBuilder(
                "WHERE ep.event_id = ? AND ep.event_participant_id <> ? AND ep.exchange_status = ? ");
        List<Object> params = new ArrayList<>();
        params.add(bean.getEventId());
        params.add(bean.getMyEventParticipantId());
        params.add(WANT);

        if (keyword != null && !keyword.isEmpty()) {
            where.append("AND p.nickname ILIKE ? ");
            params.add("%" + escapeLike(keyword) + "%");
        }
        if (ageGroup != null && !ageGroup.isEmpty()) {
            where.append("AND p.age_group = ? ");
            params.add(ageGroup);
        }
        if (interestId > 0) {
            where.append("AND EXISTS (SELECT 1 FROM public.t009_participant_interests pi "
                       + "WHERE pi.event_id = ep.event_id AND pi.participant_id = ep.participant_id "
                       + "AND pi.interest_id = ?) ");
            params.add(interestId);
        }

        String from = "FROM public.t006_events_participant ep "
                    + "JOIN public.t007_profiles p ON p.profile_id = ep.profile_id ";

        try (Connection con = getConnection()) {

            // 1. 人数
            int hit;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) " + from + where)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    hit = rs.getInt(1);
                }
            }

            // 2. 一覧（新しく参加した人から）
            String sql = "SELECT ep.event_participant_id, ep.participant_id, p.nickname, p.age_group, "
                       + "p.self_introduction, p.profile_image_url "
                       + from + where + "ORDER BY ep.event_participant_id DESC"
                       + (limit > 0 ? " LIMIT " + limit : "");

            Map<Long, ParticipantListBean.Participant> byParticipantId = new LinkedHashMap<>();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ParticipantListBean.Participant p = new ParticipantListBean.Participant();
                        p.setEventParticipantId(rs.getLong("event_participant_id"));
                        p.setParticipantId(rs.getLong("participant_id"));
                        p.setNickname(rs.getString("nickname"));
                        p.setAgeGroup(rs.getString("age_group"));
                        p.setSelfIntroduction(rs.getString("self_introduction"));
                        p.setProfileImageUrl(rs.getString("profile_image_url"));
                        byParticipantId.put(p.getParticipantId(), p);
                    }
                }
            }

            // 3. 表示する人の興味をまとめて読む
            if (!byParticipantId.isEmpty()) {
                String interestSql = "SELECT pi.participant_id, i.interest_name "
                                   + "FROM public.t009_participant_interests pi "
                                   + "JOIN public.t008_interests i ON i.interest_id = pi.interest_id "
                                   + "WHERE pi.event_id = ? AND pi.participant_id = ANY (?) "
                                   + "ORDER BY i.interest_id";
                try (PreparedStatement ps = con.prepareStatement(interestSql)) {
                    Array ids = con.createArrayOf("bigint", byParticipantId.keySet().toArray());
                    ps.setLong(1, bean.getEventId());
                    ps.setArray(2, ids);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ParticipantListBean.Participant p = byParticipantId.get(rs.getLong("participant_id"));
                            if (p != null) {
                                p.getInterests().add(rs.getString("interest_name"));
                            }
                        }
                    }
                }
            }

            bean.setParticipants(new ArrayList<>(byParticipantId.values()));
            bean.setHitCount(hit);
            bean.setOtherCount(Math.max(0, hit - byParticipantId.size()));
        }
    }

    /** 「趣味」の選択肢：このイベントで「交流したい」参加者（自分以外）が登録している興味 */
    public List<InterestBean> findInterestOptions(long eventId, long myEventParticipantId) throws SQLException {
        String sql = "SELECT DISTINCT i.interest_id, i.interest_name "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t009_participant_interests pi "
                   + "  ON pi.event_id = ep.event_id AND pi.participant_id = ep.participant_id "
                   + "JOIN public.t008_interests i ON i.interest_id = pi.interest_id "
                   + "WHERE ep.event_id = ? AND ep.event_participant_id <> ? "
                   + "  AND ep.exchange_status = ? AND i.is_active = true "
                   + "ORDER BY i.interest_id";

        List<InterestBean> list = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setLong(2, myEventParticipantId);
            ps.setString(3, WANT);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InterestBean b = new InterestBean();
                    b.setInterestId(rs.getLong("interest_id"));
                    b.setInterestName(rs.getString("interest_name"));
                    b.setActive(true);
                    list.add(b);
                }
            }
        }
        return list;
    }

    // ---------- 部品 ----------

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            if (v instanceof Long) {
                ps.setLong(i + 1, (Long) v);
            } else {
                ps.setString(i + 1, (String) v);
            }
        }
    }

    /** ILIKE の % と _ を普通の文字として扱う */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
