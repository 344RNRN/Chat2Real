package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import Bean.EventReportBean;

/**
 * 終了レポート画面（EventReport）の DAO
 *
 * 使うテーブル：
 *   t006_events_participant / t003_events / t002_meetup
 *   t004_survey_questions / t004_2_survey_answers
 */
public class EventReportDAO {

    /** 「直接交流できた」とみなす t002_meetup.meetup_result の値 ※チームの値に合わせる */
    public static final String MEETUP_SUCCESS = "成立";

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    // =========================================================
    // 参加者の終了レポート（参加者が見つからないときは null）
    // =========================================================
    public EventReportBean findReport(long eventParticipantId) throws SQLException {
        String sql = "SELECT ep.event_participant_id, e.event_id, e.event_name, e.event_status "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t003_events e ON e.event_id = ep.event_id "
                   + "WHERE ep.event_participant_id = ?";

        try (Connection con = getConnection()) {
            EventReportBean bean;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, eventParticipantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    bean = new EventReportBean();
                    bean.setEventParticipantId(rs.getLong("event_participant_id"));
                    bean.setEventId(rs.getLong("event_id"));
                    bean.setEventName(rs.getString("event_name"));
                    bean.setEventStatus(rs.getString("event_status"));
                }
            }

            bean.setMeetupCount(countMeetups(con, eventParticipantId));
            bean.setAnswered(hasAnswered(con, eventParticipantId));
            loadQuestions(con, bean);
            return bean;
        }
    }

    // =========================================================
    // 参加者がいないときの確認用（イベント名と質問だけ。送信はできない）
    // =========================================================
    public EventReportBean findPreview(long eventId) throws SQLException {
        String sql = "SELECT event_id, event_name, event_status FROM public.t003_events WHERE event_id = ?";

        try (Connection con = getConnection()) {
            EventReportBean bean;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    bean = new EventReportBean();
                    bean.setEventParticipantId(0);   // 0 = 参加者なし
                    bean.setEventId(rs.getLong("event_id"));
                    bean.setEventName(rs.getString("event_name"));
                    bean.setEventStatus(rs.getString("event_status"));
                }
            }

            loadQuestions(con, bean);
            return bean;
        }
    }

    /** いちばん最近作られたイベントの ID。イベントが無ければ null */
    public Long findLatestEventId() throws SQLException {
        String sql = "SELECT event_id FROM public.t003_events ORDER BY created_at DESC, event_id DESC LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong("event_id") : null;
        }
    }

    /** そのイベントでいちばん最近の参加者。いなければ null */
    public Long findLatestParticipantId(long eventId) throws SQLException {
        String sql = "SELECT event_participant_id FROM public.t006_events_participant "
                   + "WHERE event_id = ? ORDER BY event_participant_id DESC LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("event_participant_id") : null;
            }
        }
    }

    // =========================================================
    // ⑪ アンケートを送信：回答をまとめて登録
    // すでに回答済みなら false（二重送信の防止）
    // =========================================================
    public boolean saveAnswers(long eventParticipantId, List<EventReportBean.Answer> answers)
            throws SQLException {

        String sql = "INSERT INTO public.t004_2_survey_answers "
                   + "(event_participant_id, question_id, answer_text) VALUES (?, ?, ?)";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                if (hasAnswered(con, eventParticipantId)) {
                    con.rollback();
                    return false;
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (EventReportBean.Answer a : answers) {
                        ps.setLong(1, eventParticipantId);
                        ps.setLong(2, a.getQuestionId());
                        ps.setString(3, a.getAnswerText());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- 以下、部品 ----------

    /** ①② 直接交流できた人数 */
    private int countMeetups(Connection con, long eventParticipantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM public.t002_meetup "
                   + "WHERE event_participant_id = ? AND meetup_result = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventParticipantId);
            ps.setString(2, MEETUP_SUCCESS);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** すでに回答済みか */
    private boolean hasAnswered(Connection con, long eventParticipantId) throws SQLException {
        String sql = "SELECT 1 FROM public.t004_2_survey_answers WHERE event_participant_id = ? LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** 質問を表示順で読み込む（運営者の質問はリストへ、固定質問は ID だけ） */
    private void loadQuestions(Connection con, EventReportBean bean) throws SQLException {
        String sql = "SELECT question_id, question_text, question_type, is_required, options "
                   + "FROM public.t004_survey_questions WHERE event_id = ? "
                   + "ORDER BY display_order, question_id";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, bean.getEventId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("question_id");
                    String type = rs.getString("question_type");

                    if ("rating".equals(type)) {
                        bean.setRatingQuestionId(id);
                    } else if ("complaint".equals(type)) {
                        bean.setComplaintQuestionId(id);
                    } else {
                        EventReportBean.Question q = new EventReportBean.Question();
                        q.setQuestionId(id);
                        q.setQuestionText(rs.getString("question_text"));
                        q.setQuestionType(type);
                        q.setRequired(rs.getBoolean("is_required"));
                        q.setOptions(rs.getString("options"));
                        bean.getQuestions().add(q);
                    }
                }
            }
        }
    }
}