
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Bean.ChatBean;

public class ChatDAO {

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

    // チャットを新規登録
    public long insertChat(ChatBean chat) throws SQLException {

        String sql =
                "INSERT INTO t010_chats " +
                "(event_id, chat_type, chat_status) " +
                "VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, chat.getEventId());
            ps.setString(2, chat.getChatType());
            ps.setString(3, chat.getChatStatus());

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

    // チャットIDからチャット情報を取得
    public ChatBean findByChatId(long chatId)
            throws SQLException {

        String sql =
                "SELECT chat_id, event_id, chat_type, chat_status " +
                "FROM t010_chats WHERE chat_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    ChatBean chat = new ChatBean();

                    chat.setChatId(rs.getLong("chat_id"));
                    chat.setEventId(rs.getLong("event_id"));
                    chat.setChatType(rs.getString("chat_type"));
                    chat.setChatStatus(rs.getString("chat_status"));

                    return chat;
                }
            }
        }

        return null;
    }

    // チャットのステータスを更新
    public boolean updateChatStatus(ChatBean chat)
            throws SQLException {

        String sql =
                "UPDATE t010_chats " +
                "SET chat_status = ? WHERE chat_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, chat.getChatStatus());
            ps.setLong(2, chat.getChatId());

            return ps.executeUpdate() > 0;
        }
    }
}
