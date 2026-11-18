
package participant;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import DAO.ChatDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.TranslationService;

@WebServlet("/chat/translate")
public class TranslationAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            // セッション確認
            HttpSession session = request.getSession(false);
            
            System.out.println("===== 翻訳セッション確認 =====");
            System.out.println("Session ID = "
                    + (session == null ? "null" : session.getId()));

            System.out.println("eventParticipantId = "
                    + (session == null ? "null"
                            : session.getAttribute("eventParticipantId")));

            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\":\"ログイン情報がありません。\"}");
                return;
            }

            Object participantIdObject =
                    session.getAttribute("eventParticipantId");

            if (!(participantIdObject instanceof Number)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\":\"参加者情報がありません。\"}");
                return;
            }

            long myParticipantId =
                    ((Number) participantIdObject).longValue();

            // パラメータ取得
            long chatId;
            long messageId;

            try {
                chatId = Long.parseLong(request.getParameter("chatId"));
                messageId = Long.parseLong(request.getParameter("messageId"));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"リクエストが正しくありません。\"}");
                return;
            }

            if (chatId <= 0 || messageId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"指定された情報が正しくありません。\"}");
                return;
            }

            try {
            	System.out.println("① DAO開始");
                ChatDAO dao = new ChatDAO();

                // 自分が参加しているチャットの受信メッセージだけ取得
                String originalText = dao.findIncomingMessageText(
                        chatId,
                        messageId,
                        myParticipantId);
                
                System.out.println("② メッセージ取得完了: "
                        + (originalText == null ? "null" : "取得成功"));

                if (originalText == null) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"このメッセージは翻訳できません。\"}");
                    return;
                }

                // 翻訳実行
                String translatedText;

                try {
                    System.out.println("Google翻訳API呼び出し中...");
                    translatedText = TranslationService.translate(originalText);
                    System.out.println("Google翻訳API呼び出し成功！");

                } catch (Throwable e) {
                    System.err.println("===== Google翻訳APIエラー =====");
                    System.err.println("エラーの種類: " + e.getClass().getName());
                    System.err.println("エラー内容: " + e.getMessage());
                    e.printStackTrace(System.err);

                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\":\"Google翻訳APIでエラーが発生しました。\"}");
                    return;
                }

                // JSON形式で返す（1回だけ）
                out.print("{\"translatedText\":\""
                        + escapeJson(translatedText) + "\"}");

                System.out.println("④ Google翻訳完了");
                
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\""
                        + escapeJson(e.getMessage()) + "\"}");

            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"メッセージの取得に失敗しました。\"}");

            }  catch (Exception e) {

                System.err.println("===== 翻訳処理エラー =====");
                System.err.println("例外の種類: " + e.getClass().getName());
                System.err.println("エラー内容: " + e.getMessage());

                e.printStackTrace(System.err);

                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"翻訳に失敗しました。\"}");
            }
        }
    }

    // JSON用の文字列エスケープ
    private static String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }

        return sb.toString();
    }
}
