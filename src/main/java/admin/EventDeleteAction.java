package admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

import Bean.AccountBean;
import Bean.AdminLogin;
import DAO.EventDashboardDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** イベント一覧の「削除」（POST /admin/EventDelete.action）→ ダッシュボードに戻る。ログインチェックなし */
@WebServlet("/admin/EventDelete.action")
public class EventDeleteAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String back = request.getContextPath() + "/admin/EventDashboard.action";
        HttpSession session = request.getSession();

        Long eventId = parseLong(request.getParameter("eventId"));
        AdminLogin admin = getAdmin(request);
        if (eventId == null || admin == null) {
            response.sendRedirect(back);
            return;
        }

        try {
            String name = new EventDashboardDAO().delete(eventId, admin.getAccountId());
            session.setAttribute("dashboardFlash",
                    name == null ? "イベントが見つかりませんでした" : "「" + name + "」を削除しました");
        } catch (SQLException e) {
            throw new ServletException("イベントを削除できませんでした", e);
        }

        // 消したイベントが選ばれていたら外す（ダッシュボードは最新のイベントを表示）
        Object current = session.getAttribute("eventId");
        if (current instanceof Number && ((Number) current).longValue() == eventId) {
            session.removeAttribute("eventId");
        }
        response.sendRedirect(back);
    }

    /** URL を直接開いたときは消さずにダッシュボードへ */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/EventDashboard.action");
    }

    /** EventDashboardAction と同じ：セッションの運営者、無ければ最初のアカウント */
    private AdminLogin getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                Object value = session.getAttribute(names.nextElement());
                if (value instanceof AdminLogin) return (AdminLogin) value;
                if (value instanceof AccountBean) {
                    AccountBean a = (AccountBean) value;
                    return new AdminLogin(a.getAccountId(), a.getLoginId(), a.getAccountName());
                }
            }
        }
        try {
            return new EventDashboardDAO().findFirstAccount();
        } catch (SQLException e) {
            return null;
        }
    }

    private static Long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}