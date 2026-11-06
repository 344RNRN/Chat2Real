package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Bean.SurveyResultBean;

/**
 * アンケート結果画面の DAO
 *
 * 使うテーブル：
 *   t003_events / t006_events_participant
 *   t004_survey_questions / t004_2_survey_answers
 */
public class SurveyResultDAO {

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    /** いちばん最近作られたイベントの ID（無ければ null） */
    public Long findLatestEventId() throws SQLException {
        String sql = "SELECT event_id FROM public.t003_events ORDER BY created_at DESC, event_id DESC LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : null;
        }
    }

    /**
     * イベントのアンケート結果をまとめて読む（イベントが無ければ null）
     */
    public SurveyResultBean findResult(long eventId) throws SQLException {
        try (Connection con = getConnection()) {

            // 1. イベント名
            SurveyResultBean bean = new SurveyResultBean();
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT event_id, event_name FROM public.t003_events WHERE event_id = ?")) {
                ps.setLong(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    bean.setEventId(rs.getLong("event_id"));
                    bean.setEventName(rs.getString("event_name"));
                }
            }

            // 2. 参加者数
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM public.t006_events_participant WHERE event_id = ?")) {
                ps.setLong(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    bean.setParticipantCount(rs.getInt(1));
                }
            }

            // 3. 回答（参加者ごと・質問の表示順）
            String sql = "SELECT a.event_participant_id, a.answer_text, a.answered_at, "
                       + "       q.question_text, q.question_type "
                       + "FROM public.t004_2_survey_answers a "
                       + "JOIN public.t004_survey_questions q ON q.question_id = a.question_id "
                       + "WHERE q.event_id = ? "
                       + "ORDER BY a.event_participant_id, q.display_order, q.question_id";

            Map<Long, SurveyResultBean.Row> rows = new LinkedHashMap<>();
            int[] ratingCounts = new int[6];

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long epId = rs.getLong("event_participant_id");
                        SurveyResultBean.Row row = rows.computeIfAbsent(epId, id -> {
                            SurveyResultBean.Row r = new SurveyResultBean.Row();
                            r.setEventParticipantId(id);
                            return r;
                        });

                        // 回答日時はその人のいちばん新しいもの
                        Timestamp at = rs.getTimestamp("answered_at");
                        if (at != null && (row.getAnsweredAt() == null || at.after(row.getAnsweredAt()))) {
                            row.setAnsweredAt(at);
                        }

                        String type = rs.getString("question_type");
                        String text = rs.getString("answer_text");

                        if ("rating".equals(type)) {
                            if (text != null && text.matches("[1-5]")) {
                                int star = Integer.parseInt(text);
                                row.setRating(star);
                                ratingCounts[star]++;
                            }
                        } else if ("complaint".equals(type)) {
                            row.setComplaint(text);
                        } else {
                            row.getAnswers().add(new String[] { rs.getString("question_text"), text });
                        }
                    }
                }
            }

            // 新しい順に並べる
            List<SurveyResultBean.Row> list = new ArrayList<>(rows.values());
            list.sort(Comparator.comparing(SurveyResultBean.Row::getAnsweredAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));

            bean.setRows(list);
            bean.setRatingCounts(ratingCounts);
            return bean;
        }
    }
}