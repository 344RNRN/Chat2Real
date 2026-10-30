package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Bean.NotificationBean;

public class NotificationDAO {

    // PostgreSQL接続設定
    private static final String DB_URL =
            "jdbc:postgresql://localhost:5432/chat2real";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "pass";

    // データベース接続
    private Connection getConnection() throws SQLException {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "PostgreSQL JDBCドライバーが見つかりません。", e);
        }

        return DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD);
    }

    // お知らせ登録
    public boolean insertNotification(NotificationBean notification)
            throws SQLException {

        String sql =
                "INSERT INTO t013_notifications " +
                "(event_id, account_id, title, notification_text) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, notification.getEventId());
            pstmt.setLong(2, notification.getAccountId());
            pstmt.setString(3, notification.getTitle());
            pstmt.setString(4, notification.getNotificationText());

            return pstmt.executeUpdate() > 0;
        }
    }

    // イベントIDからお知らせ一覧を取得
    public List<NotificationBean> findByEventId(long eventId)
            throws SQLException {

        List<NotificationBean> list = new ArrayList<>();

        String sql =
        		"SELECT notification_id, event_id, account_id, " +
        		"title, notification_text, created_at " +
                "FROM t013_notifications " +
                "WHERE event_id = ? " +
                "ORDER BY notification_id DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, eventId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    NotificationBean bean =
                            new NotificationBean();

                    bean.setNotificationId(
                            rs.getLong("notification_id"));

                    bean.setEventId(
                            rs.getLong("event_id"));

                    bean.setAccountId(
                            rs.getLong("account_id"));

                    bean.setTitle(
                            rs.getString("title"));

                    bean.setNotificationText(
                            rs.getString("notification_text"));
                    
                    bean.setCreatedAt(
                    		rs.getTimestamp("created_at"));

                    list.add(bean);
                }
            }
        }

        return list;
    }
 // お知らせを削除する
    public boolean deleteNotification(long notificationId, long eventId)
            throws SQLException {

        String sql =
                "DELETE FROM t013_notifications " +
                "WHERE notification_id = ? AND event_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, notificationId);
            pstmt.setLong(2, eventId);

            return pstmt.executeUpdate() > 0;
        }
    }
}
