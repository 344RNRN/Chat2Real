<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private static String h(Object value) {
        if (value == null) {
            return "";
        }
        String s = value.toString();
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
%>
<%
    // ===== サーブレット（SurveyCompleteAction）から受け取るデータ =====
    String eventName = (String) request.getAttribute("eventName");   // イベント名（無いときは null）

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>送信完了 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/participant-message.css?v=3"">
</head>
<body>

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="message-page">

    <!-- ページ見出し -->
    <section class="page-heading">
        <span class="page-eyebrow">THANK YOU</span>
        <h1 class="page-title">送信完了</h1>
<% if (eventName != null) { %>
        <p class="page-description"><%= h(eventName) %></p>
<% } %>
    </section>

    <section class="message-card">

        <!-- 1. 完了アイコン -->
        <div class="status-icon done" aria-hidden="true">✓</div>

        <!-- 2. 完了メッセージ -->
        <h2 class="message-title">アンケートを送信しました</h2>

        <!-- 3. 説明文 -->
        <p class="message-text">
            ご回答ありがとうございました。<br>
            いただいた回答は、次回のイベントづくりに活用します。
        </p>

        <!-- 4. 案内文 -->
        <p class="note">
            本日のイベントは終了しました。チャット・参加者情報は本日中に完全削除されます。
        </p>

        <!-- 5. 閉じる -->
        <button type="button" name="closeButton" id="closeButton" class="main-button">閉じる</button>
        <p class="close-hint" id="closeHint" hidden>このタブを閉じて終了してください。</p>

    </section>

</main>

<script>
    // 閉じる（イベント処理53）：タブを閉じる。
    // ブラウザが閉じさせてくれないときは、自分で閉じるように案内する
    document.getElementById("closeButton").addEventListener("click", function () {
        window.close();
        setTimeout(function () {
            document.getElementById("closeHint").hidden = false;
        }, 300);
    });
</script>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
