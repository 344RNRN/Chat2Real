package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import Bean.EventDashboardBean;

/**
 * イベント終了確認画面（19_イベント終了確認）用の DAO
 *   t003_events / t013_notifications
 */
public class EventEndDAO {

    /** 終了したイベントの状態（t003_events.event_status） */
    public static final String STATUS_ENDED = "終了";

    /** 次回イベントの案内として配信するお知らせのタイトル */
    public static final String NEXT_EVENT_TITLE = "次回イベントのご案内";

    /** 終了10分前のお知らせ（「10分後に終了する」を押したときに配信） */
    public static final String TITLE_10MIN = "まもなくイベント終了です";
    public static final String TEXT_10MIN =
            "イベント終了まであと10分です。連絡先の交換やあいさつは今のうちにどうぞ。";

    /**
     * イベントを「終了」にし、次回イベントを選んだときは案内をお知らせとして配信する
     * （どちらかが失敗したら両方取り消す）
     *
     * @return 終了できたら true（すでに終了していた・自分のイベントでないときは false）
     */
    public boolean closeEvent(long eventId, long accountId, EventDashboardBean nextEvent)
            throws SQLException {

        String updateSql = "UPDATE public.t003_events SET event_status = ? "
                         + "WHERE event_id = ? AND account_id = ? AND event_status <> ?";

        String noticeSql = "INSERT INTO public.t013_notifications "
                         + "(event_id, account_id, title, notification_text) VALUES (?, ?, ?, ?)";

        try (Connection con = DBManager.getConnection()) {
            con.setAutoCommit(false);

            try {
                // 1. イベントを終了状態にする
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, STATUS_ENDED);
                    ps.setLong(2, eventId);
                    ps.setLong(3, accountId);
                    ps.setString(4, STATUS_ENDED);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                // 2. 次回イベントの案内を配信する（選んだときだけ）
                if (nextEvent != null) {
                    String text = nextEvent.getEventName() + "\n"
                                + nextEvent.getDateText() + "\n"
                                + nvl(nextEvent.getVenue());

                    try (PreparedStatement ps = con.prepareStatement(noticeSql)) {
                        ps.setLong(1, eventId);
                        ps.setLong(2, accountId);
                        ps.setString(3, NEXT_EVENT_TITLE);
                        ps.setString(4, text);
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
     * 運営者の「10分後に終了する」：終了日時を今から10分後にして、「あと10分」のお知らせをすぐ配信する
     * （10分たつと EventCloseScheduler が自動で「終了」にして、「終了しました」を配信する）
     * nextNoticeTitle があれば、次回イベントの案内も一緒に配信する
     * ※ notice_10min_sent 列は add_event_end_notice.sql で追加する
     *
     * @return 予約できたら true（すでに終了していた・自分のイベントでないときは false）
     */
    public boolean scheduleEndIn10Minutes(long eventId, long accountId,
                                          String nextNoticeTitle, String nextNoticeText)
            throws SQLException {

        String updateSql = "UPDATE public.t003_events "
                         + "SET end_datetime = LOCALTIMESTAMP + INTERVAL '10 minutes', notice_10min_sent = true "
                         + "WHERE event_id = ? AND account_id = ? AND event_status <> ?";

        String noticeSql = "INSERT INTO public.t013_notifications "
                         + "(event_id, account_id, title, notification_text) VALUES (?, ?, ?, ?)";

        try (Connection con = DBManager.getConnection()) {
            con.setAutoCommit(false);

            try {
                // 1. 終了日時を10分後にする
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setLong(1, eventId);
                    ps.setLong(2, accountId);
                    ps.setString(3, STATUS_ENDED);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                // 2. 「あと10分」と、次回イベントの案内（選んだときだけ）を配信する
                try (PreparedStatement ps = con.prepareStatement(noticeSql)) {
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

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
