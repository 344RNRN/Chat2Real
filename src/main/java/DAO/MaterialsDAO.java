package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Bean.MaterialsBean;

/**
 * イベント資料画面（12_イベント資料）・送信完了画面（13-2）用の DAO
 *   t006_events_participant / t003_events / t013_notifications / t014_event_materials
 */
public class MaterialsDAO {

    /** 参加者のイベント名・交流状態。見つからなければ null */
    public MaterialsBean findEventInfo(long eventParticipantId) throws SQLException {
        String sql = "SELECT e.event_id, e.event_name, ep.exchange_status "
                   + "FROM public.t006_events_participant ep "
                   + "JOIN public.t003_events e ON e.event_id = ep.event_id "
                   + "WHERE ep.event_participant_id = ?";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, eventParticipantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                MaterialsBean bean = new MaterialsBean();
                bean.setEventId(rs.getLong("event_id"));
                bean.setEventName(rs.getString("event_name"));
                bean.setExchangeStatus(rs.getString("exchange_status"));
                return bean;
            }
        }
    }

    /** お知らせ・資料を新しい順に読み込んで bean に入れる */
    public void loadItems(MaterialsBean bean) throws SQLException {
        String noticeSql = "SELECT title, notification_text FROM public.t013_notifications "
                         + "WHERE event_id = ? ORDER BY notification_id DESC";

        String materialSql = "SELECT file_name, file_path, file_type, created_at "
                           + "FROM public.t014_event_materials "
                           + "WHERE event_id = ? ORDER BY created_at DESC, material_id DESC";

        try (Connection con = DBManager.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(noticeSql)) {
                ps.setLong(1, bean.getEventId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MaterialsBean.Notice n = new MaterialsBean.Notice();
                        n.setTitle(rs.getString("title"));
                        n.setText(rs.getString("notification_text"));
                        bean.getNotices().add(n);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(materialSql)) {
                ps.setLong(1, bean.getEventId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MaterialsBean.Material m = new MaterialsBean.Material();
                        m.setFileName(rs.getString("file_name"));
                        m.setFilePath(rs.getString("file_path"));
                        m.setFileType(rs.getString("file_type"));
                        m.setCreatedAt(rs.getTimestamp("created_at"));
                        bean.getMaterials().add(m);
                    }
                }
            }
        }
    }
}
