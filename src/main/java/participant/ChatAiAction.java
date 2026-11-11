package participant;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

import Bean.ChatBean;
import Bean.ChatMessageBean;
import Bean.ChatPartnerBean;
import DAO.ChatDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 個人チャット画面の「Gemini AI チャットヘルプ」
 *   POST /chat/ai  （partnerId・tone）
 *   → Gemini API に会話を渡して「会話の要点」と「おすすめの返信 3つ」を作ってもらう
 *   → Gemini の返事（JSON）をそのまま画面（chat.jsp の JavaScript）に返す
 *
 * API キーは画面には出さない。次のどちらかに書いておく：
 *   1. 環境変数 GEMINI_API_KEY（Eclipse のサーバーの起動構成 → 環境）
 *   2. WEB-INF/gemini.properties の apiKey=...   ※ このファイルは GitHub に上げない（.gitignore）
 * キーが無い・Gemini に繋がらないときは {"error":…} を返し、画面は今までの「内蔵の提案」を使う
 */
@WebServlet("/chat/ai")
public class ChatAiAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 使うモデル（gemini.properties の model= か 環境変数 GEMINI_MODEL で変えられる） */
    private static final String DEFAULT_MODEL = "gemini-3.8-flash";

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    /** AI に渡す会話の数（新しい方から） */
    private static final int MAX_MESSAGES = 20;

    /** 連打防止：同じ人は この秒数に1回まで */
    private static final long MIN_INTERVAL_MS = 3000;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final transient ChatDAO dao = new ChatDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        // ---- 設定 ----
        String apiKey = setting("GEMINI_API_KEY", "apiKey");
        String model = setting("GEMINI_MODEL", "model");
        if (model == null) {
            model = DEFAULT_MODEL;
        }
        if (apiKey == null) {
            error(resp, "no_key", "Gemini の API キーが設定されていません");
            return;
        }

        // ---- 自分（セッション）と相手 ----
        HttpSession session = req.getSession(false);
        Object v = (session == null) ? null : session.getAttribute("eventParticipantId");
        if (!(v instanceof Number)) {
            error(resp, "no_session", "参加登録が確認できません");
            return;
        }
        long myEp = ((Number) v).longValue();

        long partnerEp;
        try {
            partnerEp = Long.parseLong(req.getParameter("partnerId").trim());
        } catch (RuntimeException e) {
            error(resp, "bad_partner", "partnerId が正しくありません");
            return;
        }

        // 連打防止
        Long last = (Long) session.getAttribute("chatAiLastAt");
        long now = System.currentTimeMillis();
        if (last != null && now - last < MIN_INTERVAL_MS) {
            error(resp, "too_fast", "少し待ってからもう一度お試しください");
            return;
        }
        session.setAttribute("chatAiLastAt", now);

        String tone = toneText(req.getParameter("tone"));

        // ---- 会話を読み込む ----
        String prompt;
        try {
            ChatPartnerBean me = dao.findPartner(myEp);
            ChatPartnerBean partner = dao.findPartner(partnerEp);
            if (me == null || partner == null || me.getEventId() != partner.getEventId()) {
                error(resp, "bad_partner", "相手が見つかりません");
                return;
            }
            ChatBean chat = dao.findOrCreatePersonalChat(me.getEventId(), myEp, partnerEp);
            List<ChatMessageBean> messages = dao.findMessages(chat.getChatId(), me.getParticipantId());
            List<String> tags = dao.findCommonInterestNames(
                    me.getEventId(), me.getParticipantId(), partner.getParticipantId());

            prompt = buildPrompt(partner.getDisplayName(), tags, messages, tone);
        } catch (SQLException e) {
            error(resp, "db", "会話を読み込めませんでした");
            return;
        }

        // ---- Gemini に聞く ----
        String body = "{"
                + "\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":" + json(prompt) + "}]}],"
                + "\"generationConfig\":{\"temperature\":0.9,\"maxOutputTokens\":2048}"
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(ENDPOINT, model)))
                    .timeout(Duration.ofSeconds(25))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> res = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (res.statusCode() != 200) {
                System.out.println("[Gemini] エラー " + res.statusCode() + "：" + res.body());
                error(resp, "api_" + res.statusCode(), "Gemini から返事がありませんでした（" + res.statusCode() + "）");
                return;
            }
            // Gemini の返事をそのまま返す（文字の取り出しは chat.jsp の JavaScript で行う）
            resp.getWriter().write(res.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error(resp, "timeout", "Gemini の返事が遅すぎました");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("[Gemini] 通信できませんでした：" + e.getMessage());
            error(resp, "network", "Gemini に接続できませんでした");
        }
    }

    // ------------------------------------------------------------------

    /** AI へのお願い（日本語） */
    private static String buildPrompt(String partnerName, List<String> tags,
                                      List<ChatMessageBean> messages, String tone) {
        StringBuilder sb = new StringBuilder();
        sb.append("あなたは交流イベント用チャットアプリ「Chat2Real」の会話アシスタントです。\n")
          .append("イベント会場で知り合った参加者どうしの1対1チャットで、ユーザー（「自分」）が次に送るメッセージを提案してください。\n\n")
          .append("# 相手\n").append(partnerName).append("さん\n\n")
          .append("# 共通の興味\n").append(tags.isEmpty() ? "（まだ分からない）" : String.join("、", tags)).append("\n\n")
          .append("# これまでの会話（古い順）\n");

        int from = Math.max(0, messages.size() - MAX_MESSAGES);
        if (messages.isEmpty()) {
            sb.append("（まだメッセージはありません。最初のあいさつを考えてください）\n");
        }
        for (int i = from; i < messages.size(); i++) {
            ChatMessageBean m = messages.get(i);
            String text = m.getText() == null ? "" : m.getText().replace("\n", " ");
            if (text.length() > 200) {
                text = text.substring(0, 200) + "…";
            }
            sb.append(m.isMine() ? "自分：" : "相手：").append(text).append('\n');
        }

        sb.append("\n# 条件\n")
          .append("- 話し方：").append(tone).append('\n')
          .append("- 返信は3つ。それぞれ違う方向性にする（相手の最後のメッセージへの返事／共通の興味の話題／イベントや日常の話題）\n")
          .append("- 1つは40文字以内。自然な日本語。相手の言語が日本語以外ならその言語で\n")
          .append("- 住所・電話番号・SNSのIDなど個人情報を聞く内容、失礼な内容、恋愛の押し付けは入れない\n")
          .append("- 「会話の要点」は、これまでの会話から分かることを短く3つ（各25文字以内）\n\n")
          .append("# 出力\n")
          .append("次の形の JSON だけを出力してください（前後に説明やコードブロックを付けない）。\n")
          .append("{\"summary\":[\"…\",\"…\",\"…\"],\"suggestions\":[\"…\",\"…\",\"…\"]}");
        return sb.toString();
    }

    private static String toneText(String tone) {
        if ("polite".equals(tone)) {
            return "丁寧（です・ます調）";
        }
        if ("friendly".equals(tone)) {
            return "親しみやすい（くだけた言い方で、絵文字を少しだけ使ってよい）";
        }
        return "カジュアル（友だちに話すような、です・ますを使わない言い方）";
    }

    /** 環境変数 → WEB-INF/gemini.properties の順に探す。無ければ null */
    private String setting(String envName, String propName) {
        String v = System.getenv(envName);
        if (v != null && !v.trim().isEmpty()) {
            return v.trim();
        }
        try (InputStream in = getServletContext().getResourceAsStream("/WEB-INF/gemini.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
                String s = p.getProperty(propName);
                if (s != null && !s.trim().isEmpty()) {
                    return s.trim();
                }
            }
        } catch (IOException e) {
            // 読めなければ無いものとする
        }
        return null;
    }

    private static void error(HttpServletResponse resp, String code, String message) throws IOException {
        resp.getWriter().write("{\"error\":" + json(code) + ",\"message\":" + json(message) + "}");
    }

    /** JSON の文字列 */
    private static String json(String s) {
        if (s == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
}
