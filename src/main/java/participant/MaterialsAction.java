package participant;

import java.io.IOException;
import java.sql.SQLException;

import Bean.MaterialsBean;
import DAO.MaterialsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * イベント資料画面（画面設計書 12_イベント資料）
 * トップ画面のお知らせバナー・資料リンク、「今は交流したくない」ボタンから表示する
 */
@WebServlet("/participant/Materials.action")
public class MaterialsAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/materials.jsp";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        long myId = MeetupAction.getMyEventParticipantId(request);

        MaterialsDAO dao = new MaterialsDAO();

        try {
            // 1. 自分のイベント（見つからなければ「イベントの情報がありません」）
            MaterialsBean bean = dao.findEventInfo(myId);

            // 2. お知らせ・資料（新しい順）
            if (bean != null) {
                dao.loadItems(bean);
            }

            request.setAttribute("materials", bean);

        } catch (SQLException e) {
            throw new ServletException("お知らせ・資料を読み込めませんでした。", e);
        }

        request.getRequestDispatcher(JSP).forward(request, response);
    }
}
