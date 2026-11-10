package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Bean.QRCodeBean;

/**
 * QRコード画面の DAO（t003_events）
 */
public class QRCodeDAO {

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    /** イベントを1件（無ければ null） */
    public QRCodeBean findEvent(long eventId) throws SQLException {
        String sql = "SELECT event_id, event_name, event_datetime, venue FROM public.t003_events WHERE event_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? toBean(rs) : null;
            }
        }
    }

    /** いちばん最近作られたイベント（無ければ null） */
    public QRCodeBean findLatestEvent() throws SQLException {
        String sql = "SELECT event_id, event_name, event_datetime, venue FROM public.t003_events "
                   + "ORDER BY created_at DESC, event_id DESC LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? toBean(rs) : null;
        }
    }

    private QRCodeBean toBean(ResultSet rs) throws SQLException {
        QRCodeBean b = new QRCodeBean();
        b.setEventId(rs.getLong("event_id"));
        b.setEventName(rs.getString("event_name"));
        b.setEventDatetime(rs.getTimestamp("event_datetime"));
        b.setVenue(rs.getString("venue"));
        return b;
    }
}
