package admin;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 運営者ログアウト（画面設計書 共通部 No.7 / イベント処理3、14_2_ログアウト）
 *
 *   各画面の「ログアウト」ボタン（確認ダイアログでOKのあと）から呼ぶ。
 *   セッションを破棄して、ログアウト画面（admin/logout.jsp）を表示する
 *
 *   /admin/logout … イベント管理・イベント作成・イベント終了確認 画面
 *   /LogoutAction … お知らせ配信 画面
 */
@WebServlet({ "/admin/logout", "/LogoutAction" })
public class LogoutAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        // 1. ログイン情報（セッション）を破棄する
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 2. ログアウト画面へ（再読み込みで二重送信しないようにリダイレクト）
        response.sendRedirect(request.getContextPath() + "/admin/logout.jsp");
    }

    // URL を直接開いたときも同じ処理にする
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        doPost(request, response);
    }
}
