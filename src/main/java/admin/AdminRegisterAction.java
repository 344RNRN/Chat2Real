
package admin;

import java.io.IOException;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import Bean.AccountBean;
import DAO.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/register")
public class AdminRegisterAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
            request.getContextPath() + "/admin/adminRegister.jsp"
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 画面から受け取る
        String organizerName = request.getParameter("organizerName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String passwordConfirm = request.getParameter("passwordConfirm");

        // 1. 未入力チェック
        if (isBlank(organizerName) || isBlank(email)
                || isBlank(password) || isBlank(passwordConfirm)) {

            showError(request, response, "すべての項目を入力してください。");
            return;
        }

        organizerName = organizerName.trim();
        email = email.trim();

        // 2. 文字数チェック（DBの login_id は50文字まで）
        if (organizerName.length() > 50) {
            showError(request, response, "運営者名は50文字以内で入力してください。");
            return;
        }

        if (email.length() > 50) {
            showError(request, response, "メールアドレスは50文字以内で入力してください。");
            return;
        }

        // 3. メールアドレスの形式チェック
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showError(request, response, "メールアドレスの形式が正しくありません。");
            return;
        }

        // 4. パスワードのチェック（英字と数字を含む8文字以上）
        if (!password.matches("^(?=.*[A-Za-z])(?=.*[0-9]).{8,72}$")) {
            showError(request, response, "パスワードは英字と数字を含む8文字以上で入力してください。");
            return;
        }

        // 5. パスワード（確認）と同じかチェック
        if (!password.equals(passwordConfirm)) {
            showError(request, response, "パスワードとパスワード（確認）が一致しません。");
            return;
        }

        try {
            AccountDAO dao = new AccountDAO();

            // 6. 同じメールアドレスが登録済みかチェック
            if (dao.findByLoginId(email) != null) {
                showError(request, response, "このメールアドレスは既に登録されています。");
                return;
            }

            // 7. パスワードを暗号化（BCrypt）して登録
            AccountBean account = new AccountBean();
            account.setLoginId(email);
            account.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            account.setAccountName(organizerName);

            dao.insertAccount(account);

        } catch (SQLException e) {

            throw new ServletException(
                "運営者アカウントの登録に失敗しました。", e
            );
        }

        // 8. 登録できたら完了画面へ（表示用に運営者名とメールをセッションに一時保存）
        HttpSession session = request.getSession();
        session.setAttribute("registeredName", organizerName);
        session.setAttribute("registeredEmail", email);

        response.sendRedirect(
            request.getContextPath() + "/admin/adminRegisterComplete.jsp"
        );
    }

    // 空かどうか
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // エラーメッセージを付けてアカウント作成画面に戻る
    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("errorMessage", message);

        request.getRequestDispatcher(
            "/admin/adminRegister.jsp"
        ).forward(request, response);
    }
}
