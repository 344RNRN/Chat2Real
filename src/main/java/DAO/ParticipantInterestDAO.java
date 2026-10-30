package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.ParticipantInterestBean;

public class ParticipantInterestDAO {

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

    // 参加者の興味・関心を登録する
    public long insertParticipantInterest(
            ParticipantInterestBean participantInterest) {

        String sql =
                "INSERT INTO t009_participant_interests "
                + "(event_id, participant_id, interest_id) "
                + "VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, participantInterest.getEventId());
            ps.setLong(2, participantInterest.getParticipantId());
            ps.setLong(3, participantInterest.getInterestId());

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

    // イベント参加者の興味・関心一覧を取得する
    public List<ParticipantInterestBean> findByEventAndParticipant(
            long eventId, long participantId) {

        List<ParticipantInterestBean> interestList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM t009_participant_interests "
                + "WHERE event_id = ? AND participant_id = ? "
                + "ORDER BY participant_interest_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, participantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    interestList.add(mapParticipantInterest(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return interestList;
    }

    // 興味・関心の登録を削除する
    public boolean deleteParticipantInterest(
            long participantInterestId) {

        String sql =
                "DELETE FROM t009_participant_interests "
                + "WHERE participant_interest_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, participantInterestId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ResultSetの内容をBeanに詰める
    private ParticipantInterestBean mapParticipantInterest(
            ResultSet rs) throws SQLException {

        ParticipantInterestBean participantInterest =
                new ParticipantInterestBean();

        participantInterest.setParticipantInterestId(
                rs.getLong("participant_interest_id"));
        participantInterest.setEventId(rs.getLong("event_id"));
        participantInterest.setParticipantId(
                rs.getLong("participant_id"));
        participantInterest.setInterestId(
                rs.getLong("interest_id"));

        return participantInterest;
    }
}