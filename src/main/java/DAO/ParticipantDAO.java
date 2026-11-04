package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Bean.ParticipantBean;

public class ParticipantDAO {

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

    // 参加者を登録する
    public long insertParticipant() {

        String sql =
                "INSERT INTO t005_participant DEFAULT VALUES";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

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

    // 参加者IDから1件取得する
    public ParticipantBean findByParticipantId(long participantId) {

        String sql =
                "SELECT * FROM t005_participant "
                + "WHERE participant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, participantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ParticipantBean participant = new ParticipantBean();

                    participant.setParticipantId(
                            rs.getLong("participant_id"));

                    return participant;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 参加者を削除する
    public boolean deleteParticipant(long participantId) {

        String sql =
                "DELETE FROM t005_participant "
                + "WHERE participant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, participantId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}