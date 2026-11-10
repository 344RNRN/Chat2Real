
package participant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import Bean.MessageBean;
import DAO.ChatDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AllChatAction")
public class AllChatAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // 全体チャット画面を表示
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        //HttpSession session = request.getSession(false);
        //テスト用↓
        HttpSession session = request.getSession();

        if (session.getAttribute("participant_id") == null) {
            session.setAttribute("participant_id", 2L);
        }

        if (session.getAttribute("event_id") == null) {
            session.setAttribute("event_id", 4L);
        }
        //テスト終わり

        // ログイン情報の確認
        if (session == null
                || session.getAttribute("participant_id") == null
                || session.getAttribute("event_id") == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "ログイン情報がありません。");

            return;
        }

        // セッションから参加者IDとイベントIDを取得
        long participantId =
                ((Number) session.getAttribute("participant_id"))
                .longValue();

        long eventId =
                ((Number) session.getAttribute("event_id"))
                .longValue();

        try {
            ChatDAO dao = new ChatDAO();

            // イベントの全体チャットIDを取得
            long chatId = dao.findGroupChatId(eventId);

            // メッセージ一覧を取得
            List<MessageBean> messageList;

            if (chatId != 0) {
                messageList = dao.findMessages(chatId);
            } else {
                messageList = new java.util.ArrayList<>();
                request.setAttribute(
                        "errorMessage",
                        "全体チャットが見つかりません。");
            }

            // JSPにデータを渡す
            request.setAttribute("messageList", messageList);
            request.setAttribute("participantId", participantId);

            // chat.jspを表示
            request.getRequestDispatcher("/participants/groupChat.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // メッセージを送信
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        // ログイン情報の確認
        if (session == null
                || session.getAttribute("participant_id") == null
                || session.getAttribute("event_id") == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "ログイン情報がありません。");

            return;
        }

        // セッションから参加者IDとイベントIDを取得
        long participantId =
                ((Number) session.getAttribute("participant_id"))
                .longValue();

        long eventId =
                ((Number) session.getAttribute("event_id"))
                .longValue();

        // 入力されたメッセージを取得
        String messageText = request.getParameter("message");

        // 空白だけのメッセージは送信しない
        if (messageText == null
                || messageText.trim().isEmpty()) {

            response.sendRedirect(
                    request.getContextPath() + "/AllChatAction");

            return;
        }

        messageText = messageText.trim();

        // 500文字を超える場合は送信しない
        if (messageText.length() > 500) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "メッセージは500文字以内で入力してください。");

            return;
        }

        try {
            ChatDAO dao = new ChatDAO();

            // イベントの全体チャットIDを取得
            long chatId = dao.findGroupChatId(eventId);

            if (chatId == 0) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "全体チャットが見つかりません。");

                return;
            }

            // MessageBeanに登録情報を設定
            MessageBean message = new MessageBean();

            message.setChatId(chatId);
            message.setSenderParticipantId(participantId);
            message.setMessageText(messageText);

            // DBにメッセージを登録
            dao.insertMessage(message);

            // 登録後、チャット画面へ戻る
            response.sendRedirect(
                    request.getContextPath() + "/AllChatAction");

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
