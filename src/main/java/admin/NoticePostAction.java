
package admin;

import java.io.IOException;
import java.sql.SQLException;

import Bean.NotificationBean;
import DAO.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/NoticePostAction")
public class NoticePostAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("NoticePostActionが呼び出されました！");

        request.setCharacterEncoding("UTF-8");

        // フォームから受け取る
        String title = request.getParameter("title");
        String message = request.getParameter("message");

        // 入力チェック
        if (title == null || title.trim().isEmpty()
                || message == null || message.trim().isEmpty()) {

            request.setAttribute("errorMessage",
                    "タイトルと本文を入力してください。");

            request.getRequestDispatcher(
                    "/admin/noticePost.jsp")
                    .forward(request, response);
            return;
        }

        title = title.trim();
        message = message.trim();

        if (title.length() > 100) {

            request.setAttribute("errorMessage",
                    "タイトルは100文字以内で入力してください。");

            request.getRequestDispatcher(
                    "/admin/noticePost.jsp")
                    .forward(request, response);
            return;
        }

        // 開発用の仮ID
        // 本番ではログイン情報から取得する
        long eventId = 2L;
        long accountId = 1L;

        // Beanにデータを格納する
        NotificationBean notification = new NotificationBean();

        notification.setEventId(eventId);
        notification.setAccountId(accountId);
        notification.setTitle(title);
        notification.setNotificationText(message);

        // DAOを使ってDBに登録する
        NotificationDAO dao = new NotificationDAO();

        try {

            boolean result = dao.insertNotification(notification);

            if (result) {

                // 登録成功
                response.sendRedirect(
                        request.getContextPath()
                        + "/admin/noticePost.jsp?success=1");

            } else {

                throw new ServletException(
                        "お知らせを登録できませんでした。");
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "お知らせの登録に失敗しました。", e);
        }
    }
}
