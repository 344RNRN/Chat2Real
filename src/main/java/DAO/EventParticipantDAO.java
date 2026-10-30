package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.EventParticipantBean;

public class EventParticipantDAO {

    private static final String DB_URL =
            "jdbc:postgresql://localhost:5432/chat2real";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "pass";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }

        return DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD);
    }

    // イベント参加者を登録する
    public long insertEventParticipant(
            EventParticipantBean eventParticipant) {

        String sql =
                "INSERT INTO t006_events_participant "
                + "(event_id, participant_id, profile_id, exchange_status) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, eventParticipant.getEventId());
            ps.setLong(2, eventParticipant.getParticipantId());

            if (eventParticipant.getProfileId() == -1) {
                ps.setNull(3, java.sql.Types.BIGINT);
            } else {
                ps.setLong(3, eventParticipant.getProfileId());
            }

            ps.setString(4, eventParticipant.getExchangeStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // イベント参加者IDから1件取得する
    public EventParticipantBean findByEventParticipantId(
            long eventParticipantId) {

        String sql =
                "SELECT * FROM t006_events_participant "
                + "WHERE event_participant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventParticipantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEventParticipant(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // イベントIDから参加者一覧を取得する
    public List<EventParticipantBean> findByEventId(long eventId) {

        List<EventParticipantBean> participantList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM t006_events_participant "
                + "WHERE event_id = ? "
                + "ORDER BY event_participant_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    participantList.add(mapEventParticipant(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participantList;
    }

    // 交流したい参加者だけ取得する
    public List<EventParticipantBean> findWillingParticipants(
            long eventId) {

        List<EventParticipantBean> participantList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM t006_events_participant "
                + "WHERE event_id = ? "
                + "AND exchange_status = ? "
                + "ORDER BY event_participant_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setString(2, "交流したい");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    participantList.add(mapEventParticipant(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participantList;
    }

    // 交流状態を更新する
    public boolean updateExchangeStatus(
            EventParticipantBean eventParticipant) {

        String sql =
                "UPDATE t006_events_participant "
                + "SET exchange_status = ? "
                + "WHERE event_participant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, eventParticipant.getExchangeStatus());
            ps.setLong(2, eventParticipant.getEventParticipantId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // イベント参加者情報を削除する
    public boolean deleteEventParticipant(long eventParticipantId) {

        String sql =
                "DELETE FROM t006_events_participant "
                + "WHERE event_participant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventParticipantId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

 // ResultSetの内容をBeanに詰める
    private EventParticipantBean mapEventParticipant(ResultSet rs)
            throws SQLException {

        EventParticipantBean eventParticipant =
                new EventParticipantBean();

        eventParticipant.setEventParticipantId(
                rs.getLong("event_participant_id"));

        eventParticipant.setEventId(
                rs.getLong("event_id"));

        eventParticipant.setParticipantId(
                rs.getLong("participant_id"));

        long profileId = rs.getLong("profile_id");

        if (rs.wasNull()) {
            eventParticipant.setProfileId(-1);
        } else {
            eventParticipant.setProfileId(profileId);
        }

        eventParticipant.setExchangeStatus(
                rs.getString("exchange_status"));

        return eventParticipant;
    }
}