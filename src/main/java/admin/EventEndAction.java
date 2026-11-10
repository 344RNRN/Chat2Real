package admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import Bean.AccountBean;
import Bean.AdminLogin;
import Bean.EventDashboardBean;
import DAO.EventCloseDAO;
import DAO.EventDashboardDAO;
import DAO.EventEndDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * イベント終了確認画面（画面設計書 19_イベント終了確認 / イベント処理51・52）
 * イベント管理画面の「イベント終了」ボタンから表示する
 *   例：/admin/EventEnd.action?eventId=1
 */
@WebServlet("/admin/EventEnd.action")
public class EventEndAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/admin/eventEnd.jsp";

    // ===== 画面を表示する =====
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        if (!loadScreen(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/EventDashboard.action");
            return;
        }
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    // ===== イベントを終了する =====
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!loadScreen(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/EventDashboard.action");
            return;
        }

        AdminLogin admin = (AdminLogin) request.getAttribute("admin");
        EventDashboardBean event = (EventDashboardBean) request.getAttribute("event");
        @SuppressWarnings("unchecked")
        List<EventDashboardBean> nextEvents =
                (List<EventDashboardBean>) request.getAttribute("nextEvents");

        // 1. すでに終了している
        if (EventEndDAO.STATUS_ENDED.equals(event.getEventStatus())) {
            showError(request, response, "このイベントはすでに終了しています。");
            return;
        }

        // 2. 確認チェック（必須）
        if (!"on".equals(request.getParameter("confirmDelete"))) {
            showError(request, response, "「データが削除されることを確認しました」にチェックを入れてください。");
            return;
        }

        // 3. 次回イベント（任意。自分のイベント一覧にあるものだけ）
        EventDashboardBean nextEvent = null;
        String nextParam = request.getParameter("nextEvent");

        if (nextParam != null && !nextParam.isEmpty()) {
            for (EventDashboardBean e : nextEvents) {
                if (String.valueOf(e.getEventId()).equals(nextParam)) {
                    nextEvent = e;
                }
            }
            if (nextEvent == null) {
                showError(request, response, "次回イベントの選択が正しくありません。");
                return;
            }
        }

        // 4. 終了する
        //    「10分後に終了」：参加者に「あと10分」を配信 → 10分後に自動で終了（EventCloseScheduler）
        //    「今すぐ終了」　：すぐ終了にして「終了しました」を配信
        boolean afterTen = "10min".equals(request.getParameter("endTiming"));
        try {
            if (afterTen) {
                String nextTitle = null;
                String nextText = null;
                if (nextEvent != null) {
                    nextTitle = EventEndDAO.NEXT_EVENT_TITLE;
                    nextText = nextEvent.getEventName() + "\n" + nextEvent.getDateText()
                             + (nextEvent.getVenue() == null ? "" : "\n" + nextEvent.getVenue());
                }
                boolean ok = new EventEndDAO().scheduleEndIn10Minutes(
                        event.getEventId(), admin.getAccountId(), nextTitle, nextText);
                if (!ok) {
                    showError(request, response, "イベントを終了できませんでした。もう一度お試しください。");
                    return;
                }
                // この画面に戻って「○時○分に終了します」を表示
                response.sendRedirect(request.getContextPath()
                        + "/admin/EventEnd.action?eventId=" + event.getEventId());
                return;
            }

            boolean closed = new EventEndDAO()
                    .closeEvent(event.getEventId(), admin.getAccountId(), nextEvent);
            if (!closed) {
                showError(request, response, "イベントを終了できませんでした。もう一度お試しください。");
                return;
            }
            new EventCloseDAO().runChecks();          // 「終了しました」のお知らせをすぐ配信
        } catch (SQLException e) {
            throw new ServletException("イベントの終了処理に失敗しました。", e);
        }

        // 5. イベント管理画面（終了済み）へ
        response.sendRedirect(request.getContextPath()
                + "/admin/EventDashboard.action?eventId=" + event.getEventId());
    }

    /**
     * 画面に必要なデータを request に入れる
     * @return 表示するイベントが無ければ false
     */
    private boolean loadScreen(HttpServletRequest request) throws ServletException {

        AdminLogin admin = getAdmin(request);
        if (admin == null) {
            return false;
        }

        Long eventId = parseLong(request.getParameter("eventId"));
        if (eventId == null) {
            HttpSession session = request.getSession(false);
            Object s = (session != null) ? session.getAttribute("eventId") : null;
            if (s instanceof Long) eventId = (Long) s;
        }
        if (eventId == null) {
            return false;
        }

        EventDashboardDAO dao = new EventDashboardDAO();

        try {
            EventDashboardBean event = dao.findById(eventId, admin.getAccountId());
            if (event == null) {
                return false;
            }

            // 終了時の集計（参加者数・直接交流が成立した人・満足度）
            dao.loadKpi(event);

            // 次回イベントの候補：自分の他のイベントで、まだ終了していないもの
            List<EventDashboardBean> nextEvents = new ArrayList<>();
            for (EventDashboardBean e : dao.findAll(admin.getAccountId())) {
                if (e.getEventId() != event.getEventId()
                        && !EventEndDAO.STATUS_ENDED.equals(e.getEventStatus())) {
                    nextEvents.add(e);
                }
            }

            request.setAttribute("admin", admin);
            request.setAttribute("accountName", admin.getAccountName());
            request.setAttribute("event", event);
            request.setAttribute("nextEvents", nextEvents);
            request.setAttribute("eventEndLoaded", Boolean.TRUE);

            // 「10分後に終了」を押したあとなら終了予定時刻（例：14:35）
            request.setAttribute("scheduledEnd", new EventCloseDAO().findScheduledEndTime(event.getEventId()));
            return true;

        } catch (SQLException e) {
            throw new ServletException("イベント情報を読み込めませんでした。", e);
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response,
                           String message) throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    /**
     * 運営者を取り出す（イベント管理画面 EventDashboardAction と同じ方法）
     *   1. セッションに AdminLogin / AccountBean があればそれ
     *   2. 無ければ t001_account の最初のアカウント
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