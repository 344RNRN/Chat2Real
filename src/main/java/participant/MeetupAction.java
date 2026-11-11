package participant;

import java.io.IOException;
import java.sql.SQLException;

import Bean.EventParticipantBean;
import Bean.ExchangeRequestBean;
import Bean.ProfileBean;
import DAO.EventParticipantDAO;
import DAO.ExchangeRequestDAO;
import DAO.MeetupZoneDAO;
import DAO.ProfileDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 合流画面を表示する（画面設計書 10_合流）
 * 例：/participants/meetup?exchangeRequestId=9001
 */
@WebServlet("/participants/meetup")
public class MeetupAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // 開発用の仮ID（テストデータ meetup_test_data.sql の NYI）
    // 本番では参加者ログインのセッションから取得する
    static final long DEV_EVENT_PARTICIPANT_ID = 9001L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // 1. どの交流申請の合流か
        long exchangeRequestId = toLong(request.getParameter("exchangeRequestId"));

        // 2. 自分のイベント参加者ID
        long myId = getMyEventParticipantId(request);

        // 3. 交流申請を取得し、自分が当事者か確認する
        ExchangeRequestDAO requestDao = new ExchangeRequestDAO();
        ExchangeRequestBean exchange = (exchangeRequestId > 0)
                ? requestDao.findByExchangeRequestId(exchangeRequestId)
                : null;

        long partnerId = getPartnerId(exchange, myId);

        if (partnerId > 0) {
            try {
                EventParticipantDAO participantDao = new EventParticipantDAO();

                // 4. 相手のプロフィール（ニックネーム・写真）
                ProfileBean profile = findPartnerProfile(partnerId);

                if (profile != null) {
                    request.setAttribute("partnerName", profile.getNickname());
                    request.setAttribute("partnerPhoto", profile.getProfileImageUrl());
                }

                // 5. 相手と自分の今いるエリア
                request.setAttribute("partnerZone", participantDao.findCurrentZone(partnerId));
                request.setAttribute("myZone", participantDao.findCurrentZone(myId));
                request.setAttribute("exchangeRequestId", exchangeRequestId);

                // 6. 運営者が入れた合流エリア・会場マップ（お知らせ配信画面で設定）
                Object[] venue = new MeetupZoneDAO().findByEventParticipantId(myId);
                request.setAttribute("zones", venue[0]);
                request.setAttribute("venueMapPath", venue[1]);

            } catch (SQLException e) {
                throw new ServletException("合流情報の取得に失敗しました。", e);
            }
        }

        // 7. 画面へ（相手がいないときは「合流する相手の情報がありません。」）
        request.getRequestDispatcher("/participants/meetup.jsp")
               .forward(request, response);
    }

    // 相手のプロフィールを取得する（交流結果登録画面でも使う）
    static ProfileBean findPartnerProfile(long partnerId) throws SQLException {
        EventParticipantBean partner =
                new EventParticipantDAO().findByEventParticipantId(partnerId);

        if (partner == null) {
            return null;
        }
        return new ProfileDAO().findByParticipantId(partner.getParticipantId());
    }

    // セッションの自分のID（まだ参加者ログインが無いので、無いときは仮ID）
    static long getMyEventParticipantId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("eventParticipantId") instanceof Long) {
            return (Long) session.getAttribute("eventParticipantId");
        }

        return DEV_EVENT_PARTICIPANT_ID;
    }

    // 交流申請の「自分ではない方」が相手。自分が当事者でなければ 0
    static long getPartnerId(ExchangeRequestBean exchange, long myId) {
        if (exchange == null) {
            return 0;
        }
        if (exchange.getRequesterEventParticipantId() == myId) {
            return exchange.getTargetEventParticipantId();
        }
        if (exchange.getTargetEventParticipantId() == myId) {
            return exchange.getRequesterEventParticipantId();
        }
        return 0;
    }

    // 数字でなければ 0
    static long toLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
