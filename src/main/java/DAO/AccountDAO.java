
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Bean.AccountBean;

public class AccountDAO {

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

    // ログインIDからアカウントを取得
    public AccountBean findByLoginId(String loginId)
            throws SQLException {

        String sql =
                "SELECT account_id, login_id, password_hash, " +
                "account_name, created_at " +
                "FROM t001_account WHERE login_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, loginId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountBean account = new AccountBean();

                    account.setAccountId(rs.getLong("account_id"));
                    account.setLoginId(rs.getString("login_id"));
                    account.setPasswordHash(
                            rs.getString("password_hash"));
                    account.setAccountName(
                            rs.getString("account_name"));
                    account.setCreatedAt(
                            rs.getTimestamp("created_at"));

                    return account;
                }
            }
        }

        return null;
    }

    // アカウントを新規登録
    public long insertAccount(AccountBean account)
            throws SQLException {

        String sql =
                "INSERT INTO t001_account " +
                "(login_id, password_hash, account_name) " +
                "VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, account.getLoginId());
            ps.setString(2, account.getPasswordHash());
            ps.setString(3, account.getAccountName());

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
}
