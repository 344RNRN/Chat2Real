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

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
