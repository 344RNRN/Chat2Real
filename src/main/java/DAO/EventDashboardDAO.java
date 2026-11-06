package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Bean.AdminLogin;
import Bean.EventDashboardBean;

/**
 * イベントダッシュボードの DAO（t003_events）
 */
public class EventDashboardDAO {

    private static final String COLUMNS =
            "event_id, event_name, event_datetime, end_datetime, venue, event_status";

    /** 指定のイベント（その運営者のものだけ）。無ければ null */
    public EventDashboardBean findById(long eventId, long accountId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM public.t003_events "
                   + "WHERE event_id = ? AND account_id = ?";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setLong(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? toBean(rs) : null;
            }
        }
    }

    /** その運営者がいちばん最近作ったイベント。無ければ null */
    public EventDashboardBean findLatest(long accountId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM public.t003_events "
                   + "WHERE account_id = ? ORDER BY created_at DESC, event_id DESC LIMIT 1";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? toBean(rs) : null;
            }
        }
    }

    /** 交流申請が承認されたことを表す t012_exchange_requests.request_status の値 ※チームの値に合わせる */
    public static final String REQUEST_APPROVED = "承認";

    /** 直接交流が成立したことを表す t002_meetup.meetup_result の値 ※EventReportDAO と同じにする */
    public static final String MEETUP_SUCCESS = "成立";

    /**
     * ダッシュボードの KPI を1回の SQL で集計して bean に入れる
     *   参加者数 / チャット利用者 / 相互承認された人 / 直接交流が成立した人 / 満足度の平均・回答数
     */
    public void loadKpi(EventDashboardBean bean) throws SQLException {
        String sql =
              "SELECT "
            // 参加者数
            + " (SELECT COUNT(*) FROM public.t006_events_participant ep WHERE ep.event_id = ?) AS participants, "
            // チャットを1回以上送った人（このイベントのチャットだけ）
            + " (SELECT COUNT(DISTINCT ep.event_participant_id) "
            + "    FROM public.t006_events_participant ep "
            + "    JOIN public.t011_messages m ON m.sender_participant_id = ep.participant_id "
            + "    JOIN public.t010_chats c ON c.chat_id = m.chat_id AND c.event_id = ep.event_id "
            + "   WHERE ep.event_id = ?) AS chat_users, "
            // 交流申請が承認された人（申請した側・された側）
            + " (SELECT COUNT(DISTINCT ep.event_participant_id) "
            + "    FROM public.t006_events_participant ep "
            + "    JOIN public.t012_exchange_requests r "
            + "      ON r.requester_event_participant_id = ep.event_participant_id "
            + "      OR r.target_event_participant_id = ep.event_participant_id "
            + "   WHERE ep.event_id = ? AND r.request_status = ?) AS approved_users, "
            // 直接交流が成立した人
            + " (SELECT COUNT(DISTINCT ep.event_participant_id) "
            + "    FROM public.t006_events_participant ep "
            + "    JOIN public.t002_meetup mt ON mt.event_participant_id = ep.event_participant_id "
            + "   WHERE ep.event_id = ? AND mt.meetup_result = ?) AS meetup_users, "
            // 満足度（★1～5 の回答だけ）
            + " (SELECT AVG(CAST(a.answer_text AS numeric)) "
            + "    FROM public.t004_2_survey_answers a "
            + "    JOIN public.t004_survey_questions q ON q.question_id = a.question_id "
            + "   WHERE q.event_id = ? AND q.question_type = 'rating' AND a.answer_text ~ '^[1-5]$') AS satisfaction_avg, "
            + " (SELECT COUNT(*) "
            + "    FROM public.t004_2_survey_answers a "
            + "    JOIN public.t004_survey_questions q ON q.question_id = a.question_id "
            + "   WHERE q.event_id = ? AND q.question_type = 'rating' AND a.answer_text ~ '^[1-5]$') AS rating_count";

        long id = bean.getEventId();
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setLong(3, id);
            ps.setString(4, REQUEST_APPROVED);
            ps.setLong(5, id);
            ps.setString(6, MEETUP_SUCCESS);
            ps.setLong(7, id);
            ps.setLong(8, id);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                bean.setParticipantCount(rs.getInt("participants"));
                bean.setChatUserCount(rs.getInt("chat_users"));
                bean.setApprovedUserCount(rs.getInt("approved_users"));
                bean.setMeetupUserCount(rs.getInt("meetup_users"));
                java.math.BigDecimal avg = rs.getBigDecimal("satisfaction_avg");
                bean.setSatisfactionAvg(avg == null ? null : avg.doubleValue());
                bean.setRatingCount(rs.getInt("rating_count"));
            }
        }
    }

    /** その運営者のイベント一覧（新しい順・ダッシュボードのイベント一覧用） */
    public java.util.List<EventDashboardBean> findAll(long accountId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM public.t003_events "
                   + "WHERE account_id = ? ORDER BY event_datetime DESC, event_id DESC";

        java.util.List<EventDashboardBean> list = new java.util.ArrayList<>();
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(toBean(rs));
                }
            }
        }
        return list;
    }

    /** いちばん最初に作られた運営者アカウント（ログインしていないとき用）。無ければ null */
    public AdminLogin findFirstAccount() throws SQLException {
        String sql = "SELECT account_id, login_id, account_name FROM public.t001_account "
                   + "ORDER BY account_id LIMIT 1";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return new AdminLogin(rs.getLong("account_id"),
                                  rs.getString("login_id"),
                                  rs.getString("account_name"));
        }
    }

    private EventDashboardBean toBean(ResultSet rs) throws SQLException {
        EventDashboardBean b = new EventDashboardBean();
        b.setEventId(rs.getLong("event_id"));
        b.setEventName(rs.getString("event_name"));
        b.setEventDatetime(rs.getTimestamp("event_datetime"));
        b.setEndDatetime(rs.getTimestamp("end_datetime"));
        b.setVenue(rs.getString("venue"));
        b.setEventStatus(rs.getString("event_status"));
        return b;
    }
}