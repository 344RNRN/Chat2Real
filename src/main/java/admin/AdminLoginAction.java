
package admin;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;

import bean.AdminLogin;
import dao.AdminLoginDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/login")
public class AdminLoginAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
            request.getContextPath() + "/admin/adminLogin.jsp"
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String loginID = request.getParameter("loginID");
        String password = request.getParameter("password");

        if (loginID == null || loginID.isBlank()
                || password == null || password.isBlank()) {

            request.setAttribute(
                "errorMessage",
                "ログインIDとパスワードを入力してください。"
            );

            request.getRequestDispatcher(
                "/admin/adminLogin.jsp"
            ).forward(request, response);

            return;
        }

        try {
            AdminLoginDao dao = new AdminLoginDao();

            AdminLogin admin = dao.login(loginID, password);

            if (admin != null) {

                HttpSession session = request.getSession(true);

                // Prevent session fixation after authentication.
                request.changeSessionId();

                session.setAttribute("admin", admin);

                response.sendRedirect(
                    request.getContextPath() + "/admin/EventDashboard.jsp"
                );

            } else {

                request.setAttribute(
                    "errorMessage",
                    "ログインIDまたはパスワードが正しくありません。"
                );

                request.getRequestDispatcher(
                    "/admin/adminLogin.jsp"
                ).forward(request, response);
            }

        } catch (NamingException | SQLException e) {

            throw new ServletException(
                "運営者ログイン処理に失敗しました。", e
            );
        }
    }
}
