
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import bean.AdminLogin;
import org.mindrot.jbcrypt.BCrypt;

public class AdminLoginDao {

    private DataSource dataSource;

    public AdminLoginDao() throws NamingException {
        InitialContext context = new InitialContext();

        dataSource = (DataSource) context.lookup(
            "java:comp/env/jdbc/chat2real"
        );
    }

    public AdminLogin login(String loginId, String password)
            throws SQLException {

        String sql =
            "SELECT account_id, login_id, password_hash, account_name " +
            "FROM t001_account " +
            "WHERE login_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, loginId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    String passwordHash =
                        resultSet.getString("password_hash");

                    if (passwordHash != null
                            && BCrypt.checkpw(password, passwordHash)) {

                        AdminLogin admin = new AdminLogin();

                        admin.setAccountId(
                            resultSet.getLong("account_id")
                        );

                        admin.setLoginId(
                            resultSet.getString("login_id")
                        );

                        admin.setAccountName(
                            resultSet.getString("account_name")
                        );

                        return admin;
                    }
                }
            }
        }

        return null;
    }
}
