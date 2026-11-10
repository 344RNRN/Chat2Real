package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Bean.NotificationBean;

/**
 * お知らせ配信（t013_notifications）の DAO
 * ※ 接続はほかの DAO と同じ DBManager（context.xml の jdbc/chat2real）を使う
 * ※ created_at 列は add_notice_created_at.sql で追加する
 */
public class NotificationDAO {

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    // お知らせ登録（添付ファイルがあれば t014_event_materials にも登録。どちらか失敗したら両方取り消す）
    public boolean insertNotification(NotificationBean notification) throws SQLException {
        String sql = "INSERT INTO public.t013_notifications "
                   + "(event_id, account_id, title, notification_text) VALUES (?, ?, ?, ?) "
                   + "RETURNING notification_id";
        String fileSql = "INSERT INTO public.t014_event_materials "
                       + "(event_id, file_name, file_path, file_type, file_size, notification_id) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, notification.getEventId());
                    pstmt.setLong(2, notification.getAccountId());
                    pstmt.setString(3, notification.getTitle());
                    pstmt.setString(4, notification.getNotificationText());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        rs.next();
                        id = rs.getLong(1);
                    }
                }
                notification.setNotificationId(id);

                if (notification.hasAttachment()) {
                    try (PreparedStatement pstmt = conn.prepareStatement(fileSql)) {
                        pstmt.setLong(1, notification.getEventId());
                        pstmt.setString(2, notification.getAttachmentName());
                        pstmt.setString(3, notification.getAttachmentPath());
                        pstmt.setString(4, notification.getAttachmentType());
                        pstmt.setLong(5, notification.getAttachmentSize());
                        pstmt.setLong(6, id);
                        pstmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // イベントIDからお知らせ一覧を取得（新しい順）
    public List<NotificationBean> findByEventId(long eventId) throws SQLException {
        List<NotificationBean> list = new ArrayList<>();

        // 添付ファイル（1件目）も一緒に取る
        String sql = "SELECT n.notification_id, n.event_id, n.account_id, n.title, n.notification_text, n.created_at, "
                   + "       m.file_name, m.file_path, m.file_type "
                   + "FROM public.t013_notifications n "
                   + "LEFT JOIN LATERAL (SELECT file_name, file_path, file_type FROM public.t014_event_materials x "
                   + "                   WHERE x.notification_id = n.notification_id "
                   + "                   ORDER BY x.material_id LIMIT 1) m ON true "
                   + "WHERE n.event_id = ? ORDER BY n.notification_id DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NotificationBean bean = new NotificationBean();
                    bean.setNotificationId(rs.getLong("notification_id"));
                    bean.setEventId(rs.getLong("event_id"));
                    bean.setAccountId(rs.getLong("account_id"));
                    bean.setTitle(rs.getString("title"));
                    bean.setNotificationText(rs.getString("notification_text"));
                    bean.setCreatedAt(rs.getTimestamp("created_at"));
                    bean.setAttachmentName(rs.getString("file_name"));
                    bean.setAttachmentPath(rs.getString("file_path"));
                    bean.setAttachmentType(rs.getString("file_type"));
                    list.add(bean);
                }
            }
        }
        return list;
    }

    // お知らせを削除する（そのイベントのお知らせだけ。添付の資料行は ON DELETE CASCADE で一緒に消える）
    public boolean deleteNotification(long notificationId, long eventId) throws SQLException {
        String sql = "DELETE FROM public.t013_notifications WHERE notification_id = ? AND event_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, notificationId);
            pstmt.setLong(2, eventId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ---------- ダッシュボードとつなぐための部品 ----------

    /** イベント名と運営者ID（[0]=event_name, [1]=account_id）。無ければ null */
    public Object[] findEvent(long eventId) throws SQLException {
        String sql = "SELECT event_name, account_id FROM public.t003_events WHERE event_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, eventId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Object[] { rs.getString("event_name"), rs.getLong("account_id") };
            }
        }
    }

    /** いちばん新しいイベントID（ダッシュボードを通らずに開いたとき用）。無ければ 0 */
    public long findLatestEventId() throws SQLException {
        String sql = "SELECT event_id FROM public.t003_events ORDER BY event_id DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
}
