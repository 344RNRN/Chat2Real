package participant;

import java.io.IOException;
import java.sql.SQLException;

import Bean.ExchangeRequestBean;
import Bean.ProfileBean;
import DAO.EventDashboardDAO;
import DAO.ExchangeRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 相互承認画面（画面設計書 09_相互承認）
 * 例：/participant/MutualConsent.action?exchangeRequestId=9001
 *
 * 交流申請（t012）の request_status が「承認」のときだけ表示する
 * （「承認」はイベント管理画面の集計と同じ値：EventDashboardDAO.REQUEST_APPROVED）
 */
@WebServlet("/participant/MutualConsent.action")
public class MutualConsentAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/mutualConsent.jsp";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        long exchangeRequestId =
                MeetupAction.toLong(request.getParameter("exchangeRequestId"));
        long myId = MeetupAction.getMyEventParticipantId(request);

        // 1. 交流申請を取得し、自分が当事者か確認する
        ExchangeRequestBean exchange = (exchangeRequestId > 0)
                ? new ExchangeRequestDAO().findByExchangeRequestId(exchangeRequestId)
                : null;

        long partnerId = MeetupAction.getPartnerId(exchange, myId);

        if (partnerId > 0) {
            // 2. お互いが「直接交流したい」を選んだか
            boolean approved =
                    EventDashboardDAO.REQUEST_APPROVED.equals(exchange.getRequestStatus());

            request.setAttribute("approved", approved);
            request.setAttribute("exchangeRequestId", exchangeRequestId);

            // 3. 相手と自分の写真・ニックネーム
            try {
                ProfileBean partner = MeetupAction.findPartnerProfile(partnerId);
                ProfileBean me = MeetupAction.findPartnerProfile(myId);

                request.setAttribute("partnerName",
                        partner != null ? partner.getNickname() : "相手");
                if (partner != null) {
                    request.setAttribute("partnerPhoto", partner.getProfileImageUrl());
                }
                if (me != null) {
                    request.setAttribute("myName", me.getNickname());
                    request.setAttribute("myPhoto", me.getProfileImageUrl());
                }

            } catch (SQLException e) {
                throw new ServletException("相互承認の情報を読み込めませんでした。", e);
            }
        }

        request.getRequestDispatcher(JSP).forward(request, response);
    }
}
