package participant;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * トップ画面 No.11：退出
 *   /participant/Leave.action → セッションを消して参加者ログイン画面へ
 *   （参加登録・チャットのデータは消さない。ニックネーム＋本名でまた入れる）
 */
@WebServlet("/participant/Leave.action")
public class LeaveAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String AFTER_LEAVE = "/participant/Login.action";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        leave(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        leave(request, response);
    }

    private void leave(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + AFTER_LEAVE);
    }
}
