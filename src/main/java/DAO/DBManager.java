package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DB 接続をまとめたクラス
 * AdminRegisterDAO / EventCreateDAO / EventReportDAO はすべてここを使う
 *
 * ★ PASSWORD を、pgAdmin や psql で chat2real に接続するときのパスワードに変える
 *   （PostgreSQL をインストールしたときに決めたパスワード）
 */
public class DBManager {

    private static final String URL = "jdbc:postgresql://localhost:5432/chat2real";
    private static final String USER = "postgres";
    private static final String PASSWORD = "pass";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL のドライバ（postgresql-xx.jar）が WEB-INF/lib にありません", e);
        }
    }

    private DBManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}