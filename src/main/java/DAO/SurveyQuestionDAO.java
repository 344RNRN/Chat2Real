package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.SurveyQuestionBean;

public class SurveyQuestionDAO {

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

    // アンケート質問を登録する
    public long insertQuestion(SurveyQuestionBean question) {

        String sql =
                "INSERT INTO t004_survey_questions "
                + "(event_id, question_text, question_type, "
                + "is_required, display_order) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, question.getEventId());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getQuestionType());
            ps.setBoolean(4, question.isRequired());
            ps.setInt(5, question.getDisplayOrder());

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

    // 質問IDから1件取得する
    public SurveyQuestionBean findByQuestionId(long questionId) {

        String sql =
                "SELECT * FROM t004_survey_questions "
                + "WHERE question_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQuestion(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // イベントIDから質問一覧を取得する
    public List<SurveyQuestionBean> findByEventId(long eventId) {

        List<SurveyQuestionBean> questionList = new ArrayList<>();

        String sql =
                "SELECT * FROM t004_survey_questions "
                + "WHERE event_id = ? "
                + "ORDER BY display_order, question_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questionList.add(mapQuestion(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questionList;
    }

    // 質問内容を更新する
    public boolean updateQuestion(SurveyQuestionBean question) {

        String sql =
                "UPDATE t004_survey_questions SET "
                + "question_text = ?, "
                + "question_type = ?, "
                + "is_required = ?, "
                + "display_order = ? "
                + "WHERE question_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, question.getQuestionText());
            ps.setString(2, question.getQuestionType());
            ps.setBoolean(3, question.isRequired());
            ps.setInt(4, question.getDisplayOrder());
            ps.setLong(5, question.getQuestionId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 質問を削除する
    public boolean deleteQuestion(long questionId) {

        String sql =
                "DELETE FROM t004_survey_questions "
                + "WHERE question_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, questionId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ResultSetの内容をBeanに詰める
    private SurveyQuestionBean mapQuestion(ResultSet rs)
            throws SQLException {

        SurveyQuestionBean question = new SurveyQuestionBean();

        question.setQuestionId(rs.getLong("question_id"));
        question.setEventId(rs.getLong("event_id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setQuestionType(rs.getString("question_type"));
        question.setRequired(rs.getBoolean("is_required"));
        question.setDisplayOrder(rs.getInt("display_order"));

        return question;
    }
}