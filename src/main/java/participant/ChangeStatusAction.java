package participant;


import java.io.IOException;

import Bean.EventParticipantBean;
import DAO.EventParticipantDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 交流状態の切り替え（トップ画面 No.6・7 / イベント資料画面 No.1）
 *   イベント処理9・30：「交流したい」に更新 → 参加者一覧画面
 *   イベント処理10　 ：「今は交流したくない」に更新 → イベント資料画面
 */
@WebServlet("/participant/ChangeStatus.action")
public class ChangeStatusAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static final String WANT = "交流したい";
    static final String NOT_NOW = "今は交流したくない";

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 押されたボタンだけが送られてくる
        boolean want = request.getParameter("wantButton") != null;
        boolean notNow = request.getParameter("notNowButton") != null;

        String ctx = request.getContextPath();

        if (want == notNow) {
            response.sendRedirect(ctx + "/admin/topScreen.jsp");
            return;
        }

        EventParticipantBean me = new EventParticipantBean();
        me.setEventParticipantId(MeetupAction.getMyEventParticipantId(request));
        me.setExchangeStatus(want ? WANT : NOT_NOW);

        if (!new EventParticipantDAO().updateExchangeStatus(me)) {
            throw new ServletException("交流状態を変更できませんでした。");
        }

        if (want) {
            response.sendRedirect(ctx + "/participants/participantList.jsp");
        } else {
            response.sendRedirect(ctx + "/participant/Lounge.action");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.sendRedirect(request.getContextPath() + "/admin/topScreen.jsp");
    }
}

