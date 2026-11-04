
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.MessageBean;

public class MessageDAO {

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

    // メッセージを登録
    public long insertMessage(MessageBean message)
            throws SQLException {

        String sql =
                "INSERT INTO t011_messages " +
                "(chat_id, sender_participant_id, message_text) " +
                "VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, message.getChatId());
            ps.setLong(2, message.getSenderParticipantId());
            ps.setString(3, message.getMessageText());

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

    // チャットIDからメッセージ一覧を取得
    public List<MessageBean> findByChatId(long chatId)
            throws SQLException {

        List<MessageBean> messageList = new ArrayList<>();

        String sql =
                "SELECT message_id, chat_id, " +
                "sender_participant_id, message_text, sent_at " +
                "FROM t011_messages " +
                "WHERE chat_id = ? " +
                "ORDER BY sent_at ASC, message_id ASC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MessageBean message = new MessageBean();

                    message.setMessageId(
                            rs.getLong("message_id"));
                    message.setChatId(
                            rs.getLong("chat_id"));
                    message.setSenderParticipantId(
                            rs.getLong("sender_participant_id"));
                    message.setMessageText(
                            rs.getString("message_text"));
                    message.setSentAt(
                            rs.getTimestamp("sent_at"));

                    messageList.add(message);
                }
            }
        }

        return messageList;
    }
}
