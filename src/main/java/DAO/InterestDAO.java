
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.InterestBean;

public class InterestDAO {

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

    // 興味項目を新規登録
    public long insertInterest(InterestBean interest)
            throws SQLException {

        String sql =
                "INSERT INTO t008_interests " +
                "(interest_name, is_active) VALUES (?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, interest.getInterestName());
            ps.setBoolean(2, interest.isActive());

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

    // 興味項目IDから1件取得
    public InterestBean findByInterestId(long interestId)
            throws SQLException {

        String sql =
                "SELECT interest_id, interest_name, is_active " +
                "FROM t008_interests WHERE interest_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, interestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    InterestBean interest = new InterestBean();

                    interest.setInterestId(
                            rs.getLong("interest_id"));

                    interest.setInterestName(
                            rs.getString("interest_name"));

                    interest.setActive(
                            rs.getBoolean("is_active"));

                    return interest;
                }
            }
        }

        return null;
    }

    // 有効な興味項目一覧を取得
    public List<InterestBean> findAllActive()
            throws SQLException {

        List<InterestBean> interestList = new ArrayList<>();

        String sql =
                "SELECT interest_id, interest_name, is_active " +
                "FROM t008_interests " +
                "WHERE is_active = true " +
                "ORDER BY interest_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InterestBean interest = new InterestBean();

                interest.setInterestId(
                        rs.getLong("interest_id"));

                interest.setInterestName(
                        rs.getString("interest_name"));

                interest.setActive(
                        rs.getBoolean("is_active"));

                interestList.add(interest);
            }
        }

        return interestList;
    }

    // 興味項目の有効・無効を変更
    public boolean updateInterestStatus(InterestBean interest)
            throws SQLException {

        String sql =
                "UPDATE t008_interests " +
                "SET is_active = ? WHERE interest_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, interest.isActive());
            ps.setLong(2, interest.getInterestId());

            return ps.executeUpdate() > 0;
        }
    }
}
