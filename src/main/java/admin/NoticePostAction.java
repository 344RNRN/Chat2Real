
package admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // 開発用の仮イベントID
        // 本番ではログインセッションから取得する
        long eventId = 2L;

        NotificationDAO dao = new NotificationDAO();

        try {
            // DBからお知らせ一覧を取得
            List<NotificationBean> notificationList =
                    dao.findByEventId(eventId);

            // JSPに一覧を渡す
            request.setAttribute("notificationList", notificationList);

            // JSPを表示
            request.getRequestDispatcher(
                    "/admin/noticePost.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "お知らせ一覧の取得に失敗しました。", e);
        }
    }
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("NoticePostActionが呼び出されました！");

        request.setCharacterEncoding("UTF-8");

        // フォームから受け取る
        String title = request.getParameter("title");
        String message = request.getParameter("message");

        // 入力チェック
     // 入力チェック① 空欄チェック
        if (title == null || title.trim().isEmpty()
                || message == null || message.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/NoticePostAction?error=empty");
            return;
        }

        // 前後の空白を削除
        title = title.trim();
        message = message.trim();

        // 入力チェック② タイトルの文字数チェック
        if (title.length() > 100) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/NoticePostAction?error=length");
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
            	        + "/NoticePostAction?success=1");

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
