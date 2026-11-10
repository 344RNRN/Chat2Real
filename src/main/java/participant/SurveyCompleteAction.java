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
 * 送信完了画面（画面設計書 13-2_送信完了）
 * 終了レポート画面でアンケートを送信したあとに表示する（EventReportAction からリダイレクト）
 */
@WebServlet("/participant/SurveyComplete.action")
public class SurveyCompleteAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/surveyComplete.jsp";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        long myId = MeetupAction.getMyEventParticipantId(request);

        try {
            MaterialsBean info = new MaterialsDAO().findEventInfo(myId);

            if (info != null) {
                request.setAttribute("eventName", info.getEventName());
            }
        } catch (SQLException e) {
            throw new ServletException("イベントの情報を読み込めませんでした。", e);
        }

        request.getRequestDispatcher(JSP).forward(request, response);
    }
}
