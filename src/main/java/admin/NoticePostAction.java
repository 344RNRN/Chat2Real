
package admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/NoticePostAction")
public class NoticePostAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // PostgreSQL接続設定
    private static final String DB_URL =
            "jdbc:postgresql://localhost:5432/chat2real";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "pass";

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

            request.getRequestDispatcher("/notifiPost.jsp")
                   .forward(request, response);
            return;
        }

        title = title.trim();
        message = message.trim();

        if (title.length() > 100) {
            request.setAttribute("errorMessage",
                    "タイトルは100文字以内で入力してください。");

            request.getRequestDispatcher("/notifiPost.jsp")
                   .forward(request, response);
            return;
        }

        // ログイン時に保存したセッション情報からIDを取得する
        HttpSession session = request.getSession(false);

        /*if (session == null
                || session.getAttribute("event_id") == null
                || session.getAttribute("account_id") == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "イベントまたは主催者のログイン情報がありません。");
            return;
        }
        
        long eventId;
        long accountId;


        try {
            eventId = Long.parseLong(
                    session.getAttribute("event_id").toString());
            accountId = Long.parseLong(
                    session.getAttribute("account_id").toString());
        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ログイン情報のIDが不正です。");
            return;
        }
        */
        
     // 開発用の仮ID。本番では必ずログイン情報から取得する
        long eventId = 2L;
        long accountId = 1L;

        String sql =
                "INSERT INTO t013_notifications " +
                "(event_id, account_id, title, notification_text) " +
                "VALUES (?, ?, ?, ?)";

        try {
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection(
                        DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setLong(1, eventId);
                pstmt.setLong(2, accountId);
                pstmt.setString(3, title);
                pstmt.setString(4, message);

                pstmt.executeUpdate();
            }

            // 登録後に画面へ戻る
            response.sendRedirect(
                    request.getContextPath() + "/admin/noticePost.jsp?success=1");

        } catch (ClassNotFoundException | SQLException e) {
            throw new ServletException(
                    "お知らせの登録に失敗しました。", e);
        }
    }
}
