package admin;

import java.io.IOException;
import java.sql.SQLException;

import DAO.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/NoticeDeleteAction")
public class NoticeDeleteAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String id = request.getParameter("notificationId");

        if (id == null || id.isEmpty()) {
            response.sendRedirect(
                    request.getContextPath() + "/NoticePostAction");
            return;
        }

        try {
        	int notificationId = Integer.parseInt(id);

            // 開発用の仮イベントID
            long eventId = 2L;

            NotificationDAO dao = new NotificationDAO();

            boolean result = dao.deleteNotification(
                    notificationId, eventId);

            if (result) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/NoticePostAction?delete=success");
            } else {
                response.sendRedirect(
                        request.getContextPath()
                        + "/NoticePostAction?delete=failed");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(
                    request.getContextPath() + "/NoticePostAction");

        } catch (SQLException e) {
            throw new ServletException(
                    "お知らせの削除に失敗しました。", e);
        }
    }
}