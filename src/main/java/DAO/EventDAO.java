
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.EventBean;

public class EventDAO {

    private static final String DB_URL =
            "jdbc:postgresql://localhost:5432/chat2real";

    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "pass";

    // データベース接続
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }

        return DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD);
    }

    // イベントを新規登録
    public long insertEvent(EventBean event)
            throws SQLException {

        String sql =
                "INSERT INTO t003_events " +
                "(account_id, event_name, event_datetime, " +
                "venue, event_status, event_description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, event.getAccountId());
            ps.setString(2, event.getEventName());
            ps.setTimestamp(3, event.getEventDatetime());
            ps.setString(4, event.getVenue());
            ps.setString(5, event.getEventStatus());
            ps.setString(6, event.getEventDescription());

            int result = ps.executeUpdate();

            if (result == 0) {
                return 0;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return 0;
    }

    // イベントIDからイベント情報を取得
    public EventBean findByEventId(long eventId)
            throws SQLException {

        String sql =
                "SELECT * FROM t003_events WHERE event_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createEventBean(rs);
                }
            }
        }

        return null;
    }

    // 主催者のアカウントIDからイベント一覧を取得
    public List<EventBean> findByAccountId(long accountId)
            throws SQLException {

        List<EventBean> eventList = new ArrayList<>();

        String sql =
                "SELECT * FROM t003_events " +
                "WHERE account_id = ? " +
                "ORDER BY event_datetime DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    eventList.add(createEventBean(rs));
                }
            }
        }

        return eventList;
    }

    // イベント情報を更新
    public boolean updateEvent(EventBean event)
            throws SQLException {

        String sql =
                "UPDATE t003_events SET " +
                "event_name = ?, event_datetime = ?, " +
                "venue = ?, event_status = ?, " +
                "event_description = ? " +
                "WHERE event_id = ? AND account_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, event.getEventName());
            ps.setTimestamp(2, event.getEventDatetime());
            ps.setString(3, event.getVenue());
            ps.setString(4, event.getEventStatus());
            ps.setString(5, event.getEventDescription());
            ps.setLong(6, event.getEventId());
            ps.setLong(7, event.getAccountId());

            return ps.executeUpdate() > 0;
        }
    }

    // ResultSetからEventBeanを作成
    private EventBean createEventBean(ResultSet rs)
            throws SQLException {

        EventBean event = new EventBean();

        event.setEventId(rs.getLong("event_id"));
        event.setAccountId(rs.getLong("account_id"));
        event.setEventName(rs.getString("event_name"));
        event.setEventDatetime(
                rs.getTimestamp("event_datetime"));
        event.setVenue(rs.getString("venue"));
        event.setEventStatus(rs.getString("event_status"));
        event.setEventDescription(
                rs.getString("event_description"));
        event.setCreatedAt(rs.getTimestamp("created_at"));

        return event;
    }
}
