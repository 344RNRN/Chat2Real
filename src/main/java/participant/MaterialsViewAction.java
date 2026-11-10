package participant;

import java.io.IOException;
import java.sql.SQLException;

import Bean.LoungeBean;
import DAO.LoungeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * イベント資料だけの画面（チャットなし）
 *   GET /participant/MaterialsView.action → /participants/materialsView.jsp
 * 「交流したい」の人が、参加者一覧・個人チャット・プロフィールから資料を見るときに使う
 * ※ データは資料・全体チャット画面と同じ LoungeDAO から取る
 */
@WebServlet("/participant/MaterialsView.action")
public class MaterialsViewAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/materialsView.jsp";
    private static final String REGISTER = "/participants/accountRegister.jsp";

    /** 【開発用】セッションが空なら最新の参加登録で開く。※本番では false */
    private static final boolean DEV_FALLBACK = true;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LoungeDAO dao = new LoungeDAO();
        HttpSession session = request.getSession();

        try {
            long myId = 0L;
            Object v = session.getAttribute("eventParticipantId");
            if (v instanceof Number) {
                myId = ((Number) v).longValue();
            }
            if (myId <= 0 && DEV_FALLBACK) {
                myId = dao.findLatestEventParticipantId();
            }

            LoungeBean me = (myId > 0) ? dao.findMe(myId) : null;
            if (me == null) {
                response.sendRedirect(request.getContextPath() + REGISTER);
                return;
            }

            session.setAttribute("eventParticipantId", me.getEventParticipantId());
            session.setAttribute("eventId", me.getEventId());
            session.setAttribute("exchangeStatus", me.getExchangeStatus());

            me.setItems(dao.findItems(me.getEventId()));
            request.setAttribute("materials", me);

        } catch (SQLException e) {
            throw new ServletException("イベント資料を読み込めませんでした", e);
        }

        request.getRequestDispatcher(JSP).forward(request, response);
    }
}
