package participant;

import java.io.IOException;
import java.sql.SQLException;

import Bean.ProfileBean;
import DAO.ChatEndDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * チャット終了画面（画面設計書 07_チャット終了 / イベント処理19・21）
 *
 *   POST /participant/ChatEnd.action  chatId=…
 *        個人チャット画面の「終了」ボタン（確認ダイアログでOKのあと）から呼ぶ。
 *        チャットを「終了」にして、この画面を表示する
 *   GET  /participant/ChatEnd.action?chatId=…
 *        終了したチャットの画面を表示する（相手側もこのURLで表示できる）
 */
@WebServlet("/participant/ChatEnd.action")
public class ChatEndAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/chatEnd.jsp";

    // ===== 画面を表示する =====
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        long chatId = MeetupAction.toLong(request.getParameter("chatId"));
        long myId = MeetupAction.getMyEventParticipantId(request);

        ChatEndDAO dao = new ChatEndDAO();

        try {
            String status = (chatId > 0) ? dao.findPersonalChatStatus(chatId) : null;

            // 自分のチャットのときだけ表示する
            if (status != null && dao.isMember(chatId, myId)) {
                request.setAttribute("chatEnded",
                        ChatEndDAO.STATUS_ENDED.equals(status));

                ProfileBean partner =
                        MeetupAction.findPartnerProfile(dao.findPartnerId(chatId, myId));

                request.setAttribute("partnerName",
                        partner != null ? partner.getNickname() : "相手");
                if (partner != null) {
                    request.setAttribute("partnerPhoto", partner.getProfileImageUrl());
                }
            }

        } catch (SQLException e) {
            throw new ServletException("チャットの情報を読み込めませんでした。", e);
        }

        request.getRequestDispatcher(JSP).forward(request, response);
    }

    // ===== チャットを終了する =====
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        long chatId = MeetupAction.toLong(request.getParameter("chatId"));
        long myId = MeetupAction.getMyEventParticipantId(request);

        ChatEndDAO dao = new ChatEndDAO();

        try {
            if (chatId > 0
                    && dao.findPersonalChatStatus(chatId) != null
                    && dao.isMember(chatId, myId)) {
                dao.endChat(chatId);
            }
        } catch (SQLException e) {
            throw new ServletException("チャットを終了できませんでした。", e);
        }

        // 再読み込みで二重送信しないように GET で表示する
        response.sendRedirect(request.getContextPath()
                + "/participant/ChatEnd.action?chatId=" + chatId);
    }
}
