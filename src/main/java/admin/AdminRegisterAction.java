package admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import Bean.AdminRegisterBean;
import DAO.AdminRegisterDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 運営者アカウント作成
 *
 * GET  … adminRegister.jsp を表示
 * POST … 入力チェック → 重複チェック → パスワードを BCrypt で変換 → 登録 → ログイン画面へ
 */
@WebServlet("/admin/register")
public class AdminRegisterAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/admin/adminRegister.jsp";
    private static final String COMPLETE  = "/admin/adminRegisterComplete.jsp";

    /** JSP の maxlength と同じ */
    private static final int MAX_NAME = 50;
    private static final int MAX_EMAIL = 50;

    /** 英字と数字を1文字以上含む8文字以上（JSP の pattern と同じ） */
    private static final Pattern PASSWORD_RULE = Pattern.compile("(?=.*[A-Za-z])(?=.*[0-9]).{8,}");
    /** 簡単なメール形式チェック */
    private static final Pattern EMAIL_RULE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    // =========================================================
    // GET：画面表示
    // =========================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    // =========================================================
    // POST：アカウントを作成
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String organizerName = trim(request.getParameter("organizerName"));
        String email = trim(request.getParameter("email")).toLowerCase();
        String password = nvl(request.getParameter("password"));            // 空白も文字として扱う
        String passwordConfirm = nvl(request.getParameter("passwordConfirm"));

        // ---------- 1. 入力チェック ----------
        String error = validate(organizerName, email, password, passwordConfirm);
        if (error != null) {
            forwardWithError(request, response, error);
            return;
        }

        AdminRegisterDAO dao = new AdminRegisterDAO();
        try {
            // ---------- 2. 重複チェック ----------
            if (dao.existsLoginId(email)) {
                forwardWithError(request, response, "このメールアドレスはすでに登録されています");
                return;
            }

            // ---------- 3. 登録（パスワードは BCrypt で変換して保存） ----------
            AdminRegisterBean bean = new AdminRegisterBean();
            bean.setAccountName(organizerName);
            bean.setLoginId(email);
            bean.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));

            if (dao.insert(bean) < 0) {
                // 重複チェックの後、同じメールで先に登録された場合
                forwardWithError(request, response, "このメールアドレスはすでに登録されています");
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            forwardWithError(request, response, "アカウントの作成に失敗しました。もう一度お試しください");
            return;
        }

        // ---------- 4. ログイン画面へ（再読み込みで二重登録しないようにリダイレクト） ----------
        response.sendRedirect(request.getContextPath() + COMPLETE);
    }

    // =========================================================
    // 入力チェック（エラーがなければ null）
    // =========================================================
    private String validate(String name, String email, String password, String confirm) {
        if (name.isEmpty()) return "運営者名（団体名）を入力してください";
        if (name.length() > MAX_NAME) return "運営者名は" + MAX_NAME + "文字以内で入力してください";

        if (email.isEmpty()) return "メールアドレスを入力してください";
        if (email.length() > MAX_EMAIL) return "メールアドレスは" + MAX_EMAIL + "文字以内で入力してください";
        if (!EMAIL_RULE.matcher(email).matches()) return "メールアドレスの形式が正しくありません";

        if (password.isEmpty()) return "パスワードを入力してください";
        if (!PASSWORD_RULE.matcher(password).matches())
            return "パスワードは英字と数字を含む8文字以上で入力してください";
        // BCrypt は72バイトまでしか使わないので長すぎるものは断る
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            return "パスワードが長すぎます（半角72文字以内）";

        if (!password.equals(confirm)) return "パスワードと確認用パスワードが一致しません";

        return null;
    }

    /** 画面に戻す（運営者名・メールは JSP が getParameter で入れ直す。パスワードは空になる） */
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", error);
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}