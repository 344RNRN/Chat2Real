package participant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import Bean.ExchangeRequestBean;
import DAO.EventParticipantDAO;
import DAO.ExchangeRequestDAO;
import DAO.MeetupZoneDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 「合図を送る」ボタン（画面設計書 10_合流 No.5 / イベント処理27）
 * 選んだエリアを t006_events_participant.current_zone に保存する
 */
@WebServlet("/participants/meetupSignal")
public class MeetupSignalAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        long exchangeRequestId =
                MeetupAction.toLong(request.getParameter("exchangeRequestId"));
        String zone = request.getParameter("zone");

        long myId = MeetupAction.getMyEventParticipantId(request);

        // 自分がこの交流申請の当事者のときだけ保存する
        ExchangeRequestBean exchange = (exchangeRequestId > 0)
                ? new ExchangeRequestDAO().findByExchangeRequestId(exchangeRequestId)
                : null;

        boolean isMember = MeetupAction.getPartnerId(exchange, myId) > 0;

        // 運営者が設定したエリア以外は保存しない
        if (isMember && zone != null) {
            try {
                @SuppressWarnings("unchecked")
                List<String> zones = (List<String>) new MeetupZoneDAO().findByEventParticipantId(myId)[0];
                if (zones.contains(zone)) {
                    new EventParticipantDAO().updateCurrentZone(myId, zone);
                }
            } catch (SQLException e) {
                throw new ServletException("合図の送信に失敗しました。", e);
            }
        }

        // 合流画面に戻る（リロードで二重送信しないようにリダイレクト）
        response.sendRedirect(request.getContextPath()
                + "/participants/meetup?exchangeRequestId=" + exchangeRequestId);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.sendRedirect(request.getContextPath() + "/participants/meetup");
    }
}
