package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * イベント終了まわりの DAO
 *   ・終了日時を過ぎたイベントを「終了」にする
 *   ・終了10分前／終了 のお知らせを t013_notifications に1回だけ配信する
 *   ・参加者のイベントが終了しているか調べる（参加者機能の無効化に使う）
 * ※ add_event_end_notice.sql を先に実行しておくこと
 */
public class EventCloseDAO {

    public static final String STATUS_ENDED = "終了";

    public static final String TITLE_10MIN = "まもなくイベント終了です";
    public static final String TEXT_10MIN =
            "イベント終了まであと10分です。連絡先の交換やあいさつは今のうちにどうぞ。";

    public static final String TITLE_END = "イベントは終了しました";
    public static final String TEXT_END =
            "本日はご参加ありがとうございました。チャットなどの機能は終了しました。"
          + "よろしければ終了アンケートにご協力ください。";

    /** 1分ごとに呼ぶ：自動終了 → 10分前のお知らせ → 終了のお知らせ。配信した件数を返す */
    public int runChecks() throws SQLException {

        // 1. 終了日時を過ぎたイベントを「終了」にする
        String autoEnd =
            "UPDATE public.t003_events SET event_status = '" + STATUS_ENDED + "' " +
            "WHERE event_status <> '" + STATUS_ENDED + "' " +
            "  AND end_datetime IS NOT NULL AND end_datetime <= LOCALTIMESTAMP";

        // 2. 終了10分前のお知らせ（印を付けた行だけお知らせを入れる＝二重に配信しない）
        String notice10 =
            "WITH t AS (" +
            "  UPDATE public.t003_events SET notice_10min_sent = true " +
            "  WHERE event_status <> '" + STATUS_ENDED + "' AND notice_10min_sent = false " +
            "    AND end_datetime IS NOT NULL " +
            "    AND end_datetime > LOCALTIMESTAMP " +
            "    AND end_datetime <= LOCALTIMESTAMP + INTERVAL '10 minutes' " +
            "  RETURNING event_id, account_id) " +
            "INSERT INTO public.t013_notifications (event_id, account_id, title, notification_text) " +
            "SELECT event_id, account_id, ?, ? FROM t";

        // 3. 終了のお知らせ（自動終了・運営者の「イベント終了」どちらも）
        String noticeEnd =
            "WITH t AS (" +
            "  UPDATE public.t003_events SET notice_end_sent = true, notice_10min_sent = true " +
            "  WHERE event_status = '" + STATUS_ENDED + "' AND notice_end_sent = false " +
            "  RETURNING event_id, account_id) " +
            "INSERT INTO public.t013_notifications (event_id, account_id, title, notification_text) " +
            "SELECT event_id, account_id, ?, ? FROM t";

        int sent = 0;
        try (Connection con = DBManager.getConnection()) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate(autoEnd);
            }
            try (PreparedStatement ps = con.prepareStatement(notice10)) {
                ps.setString(1, TITLE_10MIN);
                ps.setString(2, TEXT_10MIN);
                sent += ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(noticeEnd)) {
                ps.setString(1, TITLE_END);
                ps.setString(2, TEXT_END);
                sent += ps.executeUpdate();
            }
        }
        return sent;
    }

    /**
     * 参加者のイベントの状態
     *   [0] = 終了しているか（Boolean）
     *   [1] = 終了まであと何分か（Long。終了日時が無い・終了済みなら -1）
     *   [2] = イベント名
     * 参加登録が見つからなければ null
     */
    public Object[] findStatus(long eventParticipantId) throws SQLException {
        String sql =
            "SELECT e.event_name, e.event_status, e.end_datetime " +
            "FROM public.t006_events_participant ep " +
            "JOIN public.t003_events e ON e.event_id = ep.event_id " +
            "WHERE ep.event_participant_id = ?";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventParticipantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Timestamp end = rs.getTimestamp("end_datetime");
                LocalDateTime now = LocalDateTime.now();

                boolean ended = STATUS_ENDED.equals(rs.getString("event_status"))
                        || (end != null && !end.toLocalDateTime().isAfter(now));

                long minutesLeft = -1;
                if (!ended && end != null) {
                    long seconds = Duration.between(now, end.toLocalDateTime()).getSeconds();
                    minutesLeft = (seconds + 59) / 60;          // 切り上げ
                }
                return new Object[] { ended, minutesLeft, rs.getString("event_name") };
            }
        }
    }

    /**
     * 運営者の「10分後に終了する」：終了日時を今から10分後にして、「あと10分」のお知らせをすぐ配信する
     * （10分たつと EventCloseScheduler が自動で「終了」にして、「終了しました」を配信する）
     * nextNoticeTitle / nextNoticeText があれば、そのお知らせも一緒に配信する（次回イベントの案内）
     */
    public boolean scheduleEndIn10Minutes(long eventId, long accountId,
                                          String nextNoticeTitle, String nextNoticeText) throws SQLException {
        String update =
            "UPDATE public.t003_events " +
            "SET end_datetime = LOCALTIMESTAMP + INTERVAL '10 minutes', notice_10min_sent = true " +
            "WHERE event_id = ? AND account_id = ? AND event_status <> '" + STATUS_ENDED + "'";
        String notice =
            "INSERT INTO public.t013_notifications (event_id, account_id, title, notification_text) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection con = DBManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setLong(1, eventId);
                    ps.setLong(2, accountId);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(notice)) {
                    ps.setLong(1, eventId);
                    ps.setLong(2, accountId);
                    ps.setString(3, TITLE_10MIN);
                    ps.setString(4, TEXT_10MIN);
                    ps.executeUpdate();

                    if (nextNoticeTitle != null) {
                        ps.setString(3, nextNoticeTitle);
                        ps.setString(4, nextNoticeText);
                        ps.executeUpdate();
                    }
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * 「10分後に終了する」を押したあと、まだ終了していないイベントの終了予定時刻（例：14:35）。
     * カウントダウン中でなければ null
     */
    public String findScheduledEndTime(long eventId) throws SQLException {
        String sql =
            "SELECT to_char(end_datetime, 'HH24:MI') FROM public.t003_events " +
            "WHERE event_id = ? AND event_status <> '" + STATUS_ENDED + "' " +
            "  AND notice_10min_sent = true AND end_datetime > LOCALTIMESTAMP";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }
}
