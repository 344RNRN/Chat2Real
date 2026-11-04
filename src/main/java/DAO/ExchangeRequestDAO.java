package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.ExchangeRequestBean;

public class ExchangeRequestDAO {

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

    // 交流申請を登録する
    public long insertExchangeRequest(ExchangeRequestBean request) {

        String sql =
                "INSERT INTO t012_exchange_requests "
                + "(chat_id, requester_event_participant_id, "
                + "target_event_participant_id, request_status) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, request.getChatId());
            ps.setLong(2, request.getRequesterEventParticipantId());
            ps.setLong(3, request.getTargetEventParticipantId());
            ps.setString(4, request.getRequestStatus());

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

    // 交流申請IDから1件取得する
    public ExchangeRequestBean findByExchangeRequestId(
            long exchangeRequestId) {

        String sql =
                "SELECT * FROM t012_exchange_requests "
                + "WHERE exchange_request_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, exchangeRequestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapExchangeRequest(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // チャットIDから交流申請一覧を取得する
    public List<ExchangeRequestBean> findByChatId(long chatId) {

        List<ExchangeRequestBean> requestList = new ArrayList<>();

        String sql =
                "SELECT * FROM t012_exchange_requests "
                + "WHERE chat_id = ? "
                + "ORDER BY exchange_request_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requestList.add(mapExchangeRequest(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requestList;
    }

    // 交流申請の状態を更新する
    public boolean updateRequestStatus(ExchangeRequestBean request) {

        String sql =
                "UPDATE t012_exchange_requests "
                + "SET request_status = ? "
                + "WHERE exchange_request_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, request.getRequestStatus());
            ps.setLong(2, request.getExchangeRequestId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ResultSetの内容をBeanに詰める
    private ExchangeRequestBean mapExchangeRequest(ResultSet rs)
            throws SQLException {

        ExchangeRequestBean request = new ExchangeRequestBean();

        request.setExchangeRequestId(
                rs.getLong("exchange_request_id"));
        request.setChatId(rs.getLong("chat_id"));
        request.setRequesterEventParticipantId(
                rs.getLong("requester_event_participant_id"));
        request.setTargetEventParticipantId(
                rs.getLong("target_event_participant_id"));
        request.setRequestStatus(rs.getString("request_status"));

        return request;
    }
}