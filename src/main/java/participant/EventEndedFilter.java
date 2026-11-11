package participant;

import java.io.IOException;
import java.sql.SQLException;

import DAO.EventCloseDAO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * イベントが終了したら、参加者の機能をすべて使えなくする
 *   → どの参加者画面を開いても「イベントは終了しました」画面へ
 * 終了後も使えるもの：終了アンケート・アンケート完了・終了画面・退出・ログイン
 */
@WebFilter(urlPatterns = { "/participant/*", "/participants/*", "/chat", "/chat/*", "/AllChatAction" })
public class EventEndedFilter implements Filter {

    private static final String CLOSED = "/participant/EventClosed.action";

    /** 終了後も開ける URL（コンテキストパスより後ろ） */
    private static final String[] ALLOWED = {
        CLOSED,
        "/participant/EventStatus.action",
        "/participant/EventReport.action",
        "/participant/SurveyAnswer.action",
        "/participant/SurveyComplete.action",
        "/participant/Leave.action",
        "/participant/Login.action",
        "/participant/AccountRegister.action",
        "/participants/eventReport.jsp",
        "/participants/surveyComplete.jsp",
        "/participants/eventClosed.jsp",
        "/participants/accountRegister.jsp",
        "/participants/participantLogin.jsp",
    };

    /** 終了後も開ける画面か（event-alert.jsp も EventStatus.action を通してこれで判断する） */
    public static boolean isAllowedAfterEnd(String path) {
        if (path == null) return false;
        for (String ok : ALLOWED) {
            if (path.equals(ok)) return true;
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (isAllowedAfterEnd(path)) {
            chain.doFilter(request, response);
            return;
        }

        long myId = 0L;
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("eventParticipantId") instanceof Number) {
            myId = ((Number) session.getAttribute("eventParticipantId")).longValue();
        }
        if (myId <= 0) {
            chain.doFilter(request, response);      // まだ参加登録していない人は各画面に任せる
            return;
        }

        boolean ended;
        try {
            Object[] status = new EventCloseDAO().findStatus(myId);
            ended = status != null && (Boolean) status[0];
        } catch (SQLException e) {
            ended = false;                          // DB を見られないときは止めない
        }
        if (!ended) {
            chain.doFilter(request, response);
            return;
        }

        // 画面の自動更新（チャットのメッセージ取得など）には「終了」の合図だけ返す
        String query = req.getQueryString();
        boolean polling = path.equals("/chat/messages")
                || (query != null && query.contains("part=messages"));
        if (polling) {
            resp.setHeader("X-Chat-Ended", "1");    // chat.jsp はこれで画面を移動する
            resp.setHeader("X-Event-Ended", "1");
            resp.setContentType("text/html; charset=UTF-8");
            return;
        }

        resp.sendRedirect(req.getContextPath() + CLOSED);
    }
}
