package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.MeetupBean;

public class MeetupDAO {

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

    // 合流結果を登録する
    public long insertMeetup(MeetupBean meetup) {

        String sql =
                "INSERT INTO t002_meetup "
                + "(event_participant_id, exchange_request_id, meetup_result) "
                + "VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, meetup.getEventParticipantId());

            if (meetup.getExchangeRequestId() == -1) {
                ps.setNull(2, java.sql.Types.BIGINT);
            } else {
                ps.setLong(2, meetup.getExchangeRequestId());
            }

            ps.setString(3, meetup.getMeetupResult());

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

    // 合流IDから1件取得する
    public MeetupBean findByMeetupId(long meetupId) {

        String sql =
                "SELECT * FROM t002_meetup WHERE meetup_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, meetupId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMeetup(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // イベント参加者IDから合流結果を取得する
    public List<MeetupBean> findByEventParticipantId(
            long eventParticipantId) {

        List<MeetupBean> meetupList = new ArrayList<>();

        String sql =
                "SELECT * FROM t002_meetup "
                + "WHERE event_participant_id = ? "
                + "ORDER BY meetup_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventParticipantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    meetupList.add(mapMeetup(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return meetupList;
    }

    // 合流結果を更新する
    public boolean updateMeetup(MeetupBean meetup) {

        String sql =
                "UPDATE t002_meetup SET "
                + "event_participant_id = ?, "
                + "exchange_request_id = ?, "
                + "meetup_result = ? "
                + "WHERE meetup_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, meetup.getEventParticipantId());

            if (meetup.getExchangeRequestId() == 0) {
                ps.setNull(2, java.sql.Types.BIGINT);
            } else {
                ps.setLong(2, meetup.getExchangeRequestId());
            }

            ps.setString(3, meetup.getMeetupResult());
            ps.setLong(4, meetup.getMeetupId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ResultSetの内容をBeanに詰める
    private MeetupBean mapMeetup(ResultSet rs) throws SQLException {

        MeetupBean meetup = new MeetupBean();

        meetup.setMeetupId(rs.getLong("meetup_id"));
        meetup.setEventParticipantId(
                rs.getLong("event_participant_id"));

        long exchangeRequestId = rs.getLong("exchange_request_id");

        if (rs.wasNull()) {
            meetup.setExchangeRequestId(-1);
        } else {
            meetup.setExchangeRequestId(exchangeRequestId);
        }

        meetup.setMeetupResult(rs.getString("meetup_result"));

        return meetup;
    }
}