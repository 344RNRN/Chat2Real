package admin;

import java.io.IOException;
import java.sql.SQLException;

import DAO.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 配信済みお知らせの削除（POST /NoticeDeleteAction）→ お知らせ配信画面に戻る */
@WebServlet("/NoticeDeleteAction")
public class NoticeDeleteAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        NotificationDAO dao = new NotificationDAO();

        try {
            long eventId = NoticePostAction.resolveEventId(request, dao);
            String back = request.getContextPath() + "/NoticePostAction?eventId=" + eventId;

            long notificationId;
            try {
                notificationId = Long.parseLong(request.getParameter("notificationId").trim());
            } catch (RuntimeException e) {
                response.sendRedirect(back);
                return;
            }

            boolean result = dao.deleteNotification(notificationId, eventId);
            response.sendRedirect(back + (result ? "&delete=success" : "&delete=failed"));

        } catch (SQLException e) {
            throw new ServletException("お知らせの削除に失敗しました。", e);
        }
    }
}
