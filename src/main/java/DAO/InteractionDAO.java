package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Bean.InteractionBean;

/**
 * 交流申請（t012_exchange_requests）の DAO
 *   申請中 → 承認（相互承認が成立 → 相互承認画面・合流画面へ）
 *          → 辞退（相手が「今はやめておく」）
 *          → 取消（申請した人が取り消した）
 */
public class InteractionDAO {

    public static final String PENDING = "申請中";
    public static final String APPROVED = "承認";      // EventDashboardDAO.REQUEST_APPROVED と同じ
    public static final String DECLINED = "辞退";
    public static final String CANCELED = "取消";

    /** このチャットの、2人の間のいちばん新しい申請を bean に入れる（無ければ何もしない） */
    public void loadLatest(InteractionBean b) throws SQLException {
        String sql =
            "SELECT exchange_request_id, requester_event_participant_id, request_status " +
            "FROM public.t012_exchange_requests " +
            "WHERE chat_id = ? " +
            "  AND ((requester_event_participant_id = ? AND target_event_participant_id = ?) " +
            "    OR (requester_event_participant_id = ? AND target_event_participant_id = ?)) " +
            "ORDER BY exchange_request_id DESC LIMIT 1";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, b.getChatId());
            ps.setLong(2, b.getMyId());
            ps.setLong(3, b.getPartnerId());
            ps.setLong(4, b.getPartnerId());
            ps.setLong(5, b.getMyId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    b.setExchangeRequestId(rs.getLong(1));
                    b.setRequesterId(rs.getLong(2));
                    b.setStatus(rs.getString(3));
                }
            }
        }
    }

    /** 申請する（2人の間に「申請中」「承認」が無いときだけ）。申請した ID、できなければ 0 */
    public long request(long chatId, long myId, long partnerId) throws SQLException {
        String sql =
            "INSERT INTO public.t012_exchange_requests " +
            "(chat_id, requester_event_participant_id, target_event_participant_id, request_status) " +
            "SELECT ?, ?, ?, ? " +
            "WHERE NOT EXISTS (SELECT 1 FROM public.t012_exchange_requests " +
            "  WHERE chat_id = ? AND request_status IN (?, ?) " +
            "    AND ((requester_event_participant_id = ? AND target_event_participant_id = ?) " +
            "      OR (requester_event_participant_id = ? AND target_event_participant_id = ?))) " +
            "RETURNING exchange_request_id";

        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.setLong(2, myId);
            ps.setLong(3, partnerId);
            ps.setString(4, PENDING);
            ps.setLong(5, chatId);
            ps.setString(6, PENDING);
            ps.setString(7, APPROVED);
            ps.setLong(8, myId);
            ps.setLong(9, partnerId);
            ps.setLong(10, partnerId);
            ps.setLong(11, myId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    /** 届いた申請に返事する（承認 / 辞退）。自分あての「申請中」だけ変えられる */
    public boolean answer(long exchangeRequestId, long myId, String newStatus) throws SQLException {
        String sql = "UPDATE public.t012_exchange_requests SET request_status = ? "
                   + "WHERE exchange_request_id = ? AND target_event_participant_id = ? AND request_status = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, exchangeRequestId);
            ps.setLong(3, myId);
            ps.setString(4, PENDING);
            return ps.executeUpdate() == 1;
        }
    }

    /** 自分が出した「申請中」を取り消す */
    public boolean cancel(long exchangeRequestId, long myId) throws SQLException {
        String sql = "UPDATE public.t012_exchange_requests SET request_status = ? "
                   + "WHERE exchange_request_id = ? AND requester_event_participant_id = ? AND request_status = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, CANCELED);
            ps.setLong(2, exchangeRequestId);
            ps.setLong(3, myId);
            ps.setString(4, PENDING);
            return ps.executeUpdate() == 1;
        }
    }

    // =========================================================
    // 交流申請のお知らせ（参加者一覧・プロフィールなど、どの画面でも出す）
    // =========================================================

    /** お知らせ1件 */
    public static class Notice {
        public long exchangeRequestId;
        public long partnerId;
        public String partnerName;
        /** incoming：申請が届いた / approved：相互承認が成立 / declined：自分の申請が見送られた */
        public String type;
    }

    /**
     * 自分あてのお知らせ（新しい順・最大10件）
     *   ・自分に届いている「申請中」
     *   ・自分が当事者の「承認」
     *   ・自分が送って「辞退」された申請
     * ※ t012 に日時の列が無いので「もう見たか」は画面側（ブラウザ）で覚える
     */
    public List<Notice> findNotices(long myId) throws SQLException {
        String sql =
            "SELECT r.exchange_request_id, r.request_status, r.requester_event_participant_id, " +
            "       CASE WHEN r.requester_event_participant_id = ? " +
            "            THEN r.target_event_participant_id ELSE r.requester_event_participant_id END AS partner_id, " +
            "       p.nickname " +
            "FROM public.t012_exchange_requests r " +
            "JOIN public.t006_events_participant ep ON ep.event_participant_id = " +
            "     CASE WHEN r.requester_event_participant_id = ? " +
            "          THEN r.target_event_participant_id ELSE r.requester_event_participant_id END " +
            "LEFT JOIN public.t007_profiles p ON p.profile_id = ep.profile_id " +
            "WHERE (r.request_status = ? AND r.target_event_participant_id = ?) " +
            "   OR (r.request_status = ? AND (r.requester_event_participant_id = ? OR r.target_event_participant_id = ?)) " +
            "   OR (r.request_status = ? AND r.requester_event_participant_id = ?) " +
            "ORDER BY r.exchange_request_id DESC LIMIT 10";

        List<Notice> list = new ArrayList<>();
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, myId);
            ps.setLong(2, myId);
            ps.setString(3, PENDING);
            ps.setLong(4, myId);
            ps.setString(5, APPROVED);
            ps.setLong(6, myId);
            ps.setLong(7, myId);
            ps.setString(8, DECLINED);
            ps.setLong(9, myId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notice n = new Notice();
                    n.exchangeRequestId = rs.getLong(1);
                    String status = rs.getString(2);
                    n.partnerId = rs.getLong(4);
                    n.partnerName = rs.getString(5);
                    n.type = PENDING.equals(status) ? "incoming"
                           : APPROVED.equals(status) ? "approved" : "declined";
                    list.add(n);
                }
            }
        }
        return list;
    }
}
