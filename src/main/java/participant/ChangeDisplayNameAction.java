package participant;

import java.io.IOException;
import java.sql.SQLException;

import DAO.TopScreenDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * トップ画面 No.10：表示名（ニックネーム / 本名）の切り替え
 *   POST /participant/ChangeDisplayName.action → トップ画面に戻る
 */
@WebServlet("/participant/ChangeDisplayName.action")
public class ChangeDisplayNameAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String top = request.getContextPath() + "/participant/Top.action";

        long myId = TopScreenAction.myEventParticipantId(request.getSession(false));
        String value = request.getParameter("displayName");

        if (myId > 0 && value != null) {
            try {
                new TopScreenDAO().updateDisplayName(myId, value);
            } catch (SQLException e) {
                throw new ServletException("表示名を変更できませんでした", e);
            }
        }
        response.sendRedirect(top);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/participant/Top.action");
    }
}
