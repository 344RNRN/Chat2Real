package admin;

import java.io.IOException;
import java.sql.SQLException;

import Bean.SurveyResultBean;
import DAO.SurveyResultDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * アンケート結果（運営者）
 *
 * GET … /admin/SurveyResult.action?eventId=1
 *       eventId が無ければ、ダッシュボードで表示中のイベント（セッションの eventId）
 *       それも無ければ、いちばん最近のイベント
 *
 * ログインチェックはしない
 */
@WebServlet("/admin/SurveyResult.action")
public class SurveyResultAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/admin/surveyResult.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SurveyResultDAO dao = new SurveyResultDAO();
        SurveyResultBean result = null;

        try {
            Long eventId = parseLong(request.getParameter("eventId"));
            if (eventId == null) {
                Object s = request.getSession().getAttribute("eventId");
                if (s instanceof Long) eventId = (Long) s;
            }
            if (eventId != null) {
                result = dao.findResult(eventId);
            }
            if (result == null) {
                Long latest = dao.findLatestEventId();
                if (latest != null) {
                    result = dao.findResult(latest);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("アンケート結果を読み込めませんでした", e);
        }

        if (result != null) {
            request.getSession().setAttribute("eventId", result.getEventId());
        }

        request.setAttribute("result", result);   // null = イベントがない
        request.setAttribute("resultLoaded", Boolean.TRUE);
        request.getRequestDispatcher(JSP).forward(request, response);
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