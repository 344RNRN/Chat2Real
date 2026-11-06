package admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

import Bean.AccountBean;
import Bean.AdminLogin;
import Bean.EventDashboardBean;
import DAO.EventDashboardDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * イベントダッシュボード
 *
 * 表示するイベント：
 *   1. URL の ?eventId=… があればそれ
 *   2. 無ければセッションの eventId（イベント作成直後に入る）
 *   3. それも無ければ、その運営者が最後に作ったイベント
 */
@WebServlet("/admin/EventDashboard.action")
public class EventDashboardAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/admin/EventDashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AdminLogin admin = getAdmin(request);   // ログインチェックなし
        if (admin == null) {
            // アカウントが1件も無いとき：イベントなしで表示する
            request.setAttribute("event", null);
            request.setAttribute("accountName", "");
            request.setAttribute("dashboardLoaded", Boolean.TRUE);
            request.getRequestDispatcher(JSP).forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        Long eventId = parseLong(request.getParameter("eventId"));
        if (eventId == null) {
            Object s = session.getAttribute("eventId");
            if (s instanceof Long) eventId = (Long) s;
        }

        EventDashboardDAO dao = new EventDashboardDAO();
        EventDashboardBean event;
        java.util.List<EventDashboardBean> eventList;
        try {
            event = (eventId != null) ? dao.findById(eventId, admin.getAccountId()) : null;
            if (event == null) {
                event = dao.findLatest(admin.getAccountId());
            }
            eventList = dao.findAll(admin.getAccountId());   // 画面下のイベント一覧
            if (event != null) {
                dao.loadKpi(event);                          // 参加者数・各割合・満足度
            }
        } catch (SQLException e) {
            throw new ServletException("イベント情報を読み込めませんでした", e);
        }
        request.setAttribute("eventList", eventList);

        if (event != null) {
            session.setAttribute("eventId", event.getEventId());   // 他の画面（QRコードなど）で使う
        }

        request.setAttribute("event", event);                    // null のときは「イベントがありません」
        request.setAttribute("accountName", admin.getAccountName());
        request.setAttribute("dashboardLoaded", Boolean.TRUE);
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    /**
     * 運営者を取り出す（ログインチェックはしない）
     *   1. セッションに AdminLogin / AccountBean があればそれ
     *   2. 無ければ t001_account の最初のアカウント
     *   3. アカウントが1件も無ければ null
     */
    private AdminLogin getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                Object value = session.getAttribute(names.nextElement());
                if (value instanceof AdminLogin) {
                    return (AdminLogin) value;
                }
                if (value instanceof AccountBean) {
                    AccountBean a = (AccountBean) value;
                    return new AdminLogin(a.getAccountId(), a.getLoginId(), a.getAccountName());
                }
            }
        }

        try {
            return new EventDashboardDAO().findFirstAccount();
        } catch (SQLException e) {
            System.out.println("[EventDashboard] アカウントを読み込めませんでした：" + e.getMessage());
            return null;
        }
    }

    private static Long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}