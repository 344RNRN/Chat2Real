package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Bean.AdminRegisterBean;

/**
 * 運営者アカウント作成（t001_account）の DAO
 */
public class AdminRegisterDAO {

    // ※運営者ログインの DAO と同じ URL・ユーザー・パスワードにする
    //   （チームの接続クラスがあれば getConnection() をそちらに置き換える）
    private static final String URL = "jdbc:postgresql://localhost:5432/chat2real";
    private static final String USER = "postgres";
    private static final String PASSWORD = "pass";

    /** PostgreSQL の「一意制約違反」のエラーコード */
    private static final String UNIQUE_VIOLATION = "23505";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL のドライバが見つかりません", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** そのログインID（メールアドレス）がすでに使われているか */
    public boolean existsLoginId(String loginId) throws SQLException {
        String sql = "SELECT 1 FROM public.t001_account WHERE login_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loginId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * アカウントを登録して account_id を返す
     * 同じログインIDが同時に登録された場合は -1 を返す（login_id は UNIQUE）
     */
    public long insert(AdminRegisterBean bean) throws SQLException {
        String sql = "INSERT INTO public.t001_account (login_id, password_hash, account_name) "
                   + "VALUES (?, ?, ?) RETURNING account_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bean.getLoginId());
            ps.setString(2, bean.getPasswordHash());
            ps.setString(3, bean.getAccountName());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long id = rs.getLong("account_id");
                bean.setAccountId(id);
                return id;
            }
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                return -1;
            }
            throw e;
        }
    }
}