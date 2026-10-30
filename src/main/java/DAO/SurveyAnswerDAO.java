package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Bean.SurveyAnswerBean;

public class SurveyAnswerDAO {

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

    // アンケート回答を登録する
    public long insertAnswer(SurveyAnswerBean answer) {

        String sql =
                "INSERT INTO t004_2_survey_answers "
                + "(event_participant_id, question_id, answer_text) "
                + "VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, answer.getEventParticipantId());
            ps.setLong(2, answer.getQuestionId());
            ps.setString(3, answer.getAnswerText());

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

    // 回答IDから1件取得する
    public SurveyAnswerBean findByAnswerId(long answerId) {

        String sql =
                "SELECT * FROM t004_2_survey_answers "
                + "WHERE answer_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, answerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAnswer(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // イベント参加者IDから回答一覧を取得する
    public List<SurveyAnswerBean> findByEventParticipantId(
            long eventParticipantId) {

        List<SurveyAnswerBean> answerList = new ArrayList<>();

        String sql =
                "SELECT * FROM t004_2_survey_answers "
                + "WHERE event_participant_id = ? "
                + "ORDER BY answer_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventParticipantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    answerList.add(mapAnswer(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return answerList;
    }

    // 質問IDから回答一覧を取得する
    public List<SurveyAnswerBean> findByQuestionId(long questionId) {

        List<SurveyAnswerBean> answerList = new ArrayList<>();

        String sql =
                "SELECT * FROM t004_2_survey_answers "
                + "WHERE question_id = ? "
                + "ORDER BY answer_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    answerList.add(mapAnswer(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return answerList;
    }

    // 回答内容を更新する
    public boolean updateAnswer(SurveyAnswerBean answer) {

        String sql =
                "UPDATE t004_2_survey_answers "
                + "SET answer_text = ? "
                + "WHERE answer_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, answer.getAnswerText());
            ps.setLong(2, answer.getAnswerId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 回答を削除する
    public boolean deleteAnswer(long answerId) {

        String sql =
                "DELETE FROM t004_2_survey_answers "
                + "WHERE answer_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, answerId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ResultSetの内容をBeanに詰める
    private SurveyAnswerBean mapAnswer(ResultSet rs)
            throws SQLException {

        SurveyAnswerBean answer = new SurveyAnswerBean();

        answer.setAnswerId(rs.getLong("answer_id"));
        answer.setEventParticipantId(
                rs.getLong("event_participant_id"));
        answer.setQuestionId(rs.getLong("question_id"));
        answer.setAnswerText(rs.getString("answer_text"));

        return answer;
    }
}