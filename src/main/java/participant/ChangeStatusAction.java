package participant;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import DAO.DBManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 交流状態の切り替え（トップ画面 No.6・7 / イベント資料画面 No.1）
 *   「交流したい」        → 更新して 参加者一覧へ
 *   「今は交流したくない」 → 更新して イベント資料へ
 */
@WebServlet("/participant/ChangeStatus.action")
public class ChangeStatusAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static final String WANT = "交流したい";
    static final String NOT_NOW = "今は交流したくない";

    private static final String TOP = "/participant/Top.action";
    private static final String PARTICIPANT_LIST = "/participant/ParticipantList.action";
    private static final String MATERIALS = "/participant/Lounge.action";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String ctx = request.getContextPath();

        boolean want = request.getParameter("wantButton") != null;
        boolean notNow = request.getParameter("notNowButton") != null;
        long myId = myEventParticipantId(request.getSession(false));

        if (want == notNow || myId <= 0) {
            response.sendRedirect(ctx + TOP);
            return;
        }

        String sql = "UPDATE public.t006_events_participant SET exchange_status = ? "
                   + "WHERE event_participant_id = ?";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, want ? WANT : NOT_NOW);
            ps.setLong(2, myId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServletException("交流状態を変更できませんでした。", e);
        }

        response.sendRedirect(ctx + (want ? PARTICIPANT_LIST : MATERIALS));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + TOP);
    }

    /** セッションの自分の event_participant_id（無ければ 0） */
    private static long myEventParticipantId(HttpSession session) {
        if (session == null) return 0L;
        Object v = session.getAttribute("eventParticipantId");
        return (v instanceof Number) ? ((Number) v).longValue() : 0L;
    }
}