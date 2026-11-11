package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 合流エリア・会場マップ（t003_events.meetup_zones / venue_map_path）
 * ※ 列は add_meetup_venue.sql で追加する
 * 運営者がお知らせ配信画面から入れて、参加者の合流画面に出す。
 */
public class MeetupZoneDAO {

    /** 運営者がまだ入れていないときのエリア */
    public static final List<String> DEFAULT_ZONES =
            List.of("入口付近", "ステージ前", "中央テーブル", "ドリンクコーナー");

    /** エリアは最大12個・1つ30文字まで（t006.current_zone が varchar(30)） */
    public static final int MAX_ZONES = 12;
    public static final int MAX_ZONE_LENGTH = 30;

    /** [0] エリア一覧（List<String>）  [1] 会場マップのパス（無ければ null） */
    public Object[] findByEventId(long eventId) throws SQLException {
        String sql = "SELECT meetup_zones, venue_map_path FROM public.t003_events WHERE event_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[] { toZones(rs.getString(1)), rs.getString(2) };
                }
            }
        }
        return new Object[] { DEFAULT_ZONES, null };
    }

    /** 参加者（event_participant_id）のイベントのエリア・会場マップ */
    public Object[] findByEventParticipantId(long eventParticipantId) throws SQLException {
        String sql = "SELECT e.meetup_zones, e.venue_map_path "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t003_events e ON e.event_id = ep.event_id "
                   + "WHERE ep.event_participant_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, eventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[] { toZones(rs.getString(1)), rs.getString(2) };
                }
            }
        }
        return new Object[] { DEFAULT_ZONES, null };
    }

    /**
     * 保存する
     * @param mapPath  新しい会場マップ（変えないときは null）
     * @param removeMap true なら会場マップを消す
     */
    public boolean save(long eventId, List<String> zones, String mapPath, boolean removeMap)
            throws SQLException {
        String sql = "UPDATE public.t003_events SET meetup_zones = ?, "
                   + "venue_map_path = CASE WHEN ? THEN NULL ELSE COALESCE(?, venue_map_path) END "
                   + "WHERE event_id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zones.isEmpty() ? null : String.join("\n", zones));
            ps.setBoolean(2, removeMap);
            ps.setString(3, mapPath);
            ps.setLong(4, eventId);
            return ps.executeUpdate() == 1;
        }
    }

    /** テキストエリアの入力 → エリア一覧（空行・重複を除く・長さと数を制限） */
    public static List<String> parse(String text) {
        Set<String> set = new LinkedHashSet<>();
        if (text != null) {
            for (String line : text.split("\\r?\\n|,|、")) {
                String z = line.strip();
                if (z.isEmpty()) continue;
                if (z.length() > MAX_ZONE_LENGTH) z = z.substring(0, MAX_ZONE_LENGTH);
                set.add(z);
                if (set.size() >= MAX_ZONES) break;
            }
        }
        return new ArrayList<>(set);
    }

    private static List<String> toZones(String stored) {
        List<String> list = parse(stored);
        return list.isEmpty() ? DEFAULT_ZONES : list;
    }
}
