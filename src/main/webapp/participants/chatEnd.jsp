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
    // ===== サーブレット（ChatEndAction）から受け取るデータ =====
    Boolean chatEnded = (Boolean) request.getAttribute("chatEnded");     // null：自分のチャットではない
    String partnerName = (String) request.getAttribute("partnerName");   // 相手のニックネーム
    String partnerPhoto = (String) request.getAttribute("partnerPhoto"); // 相手の写真（無いときは null）

    if (partnerName == null) partnerName = "";

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>チャット終了 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/participant-message.css">
</head>
<body>

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="message-page">

    <!-- ページ見出し -->
    <section class="page-heading">
        <span class="page-eyebrow">CHAT</span>
        <h1 class="page-title">チャット終了</h1>
    </section>

    <section class="message-card">

<% if (chatEnded == null) { %>

        <p class="empty-message">チャットの情報がありません。</p>

<% } else { %>

        <!-- 相手 -->
        <div class="person-chip">
            <span class="avatar">
<% if (partnerPhoto != null) { %>
                <img src="<%= h(partnerPhoto) %>" alt="<%= h(partnerName) %>さんの写真">
<% } else { %>
                <%= h(partnerName.substring(0, 1)) %>
<% } %>
            </span>
            <span class="person-name"><%= h(partnerName) %></span>
        </div>

<% if (chatEnded) { %>
        <!-- 1. 終了アイコン -->
        <div class="status-icon end" aria-hidden="true">✕</div>

        <!-- 2. 終了メッセージ -->
        <h2 class="message-title">チャットを終了しました</h2>

        <!-- 3. 説明文 -->
        <p class="message-text">
            お相手にも終了したことが伝わります。<br>
            ありがとうございました。
        </p>
<% } else { %>
        <h2 class="message-title">このチャットはまだ終了していません</h2>
        <p class="message-text">個人チャット画面の「終了」ボタンから終了できます。</p>
<% } %>

<% } %>

        <!-- 4. 参加者一覧に戻る -->
        <form action="<%= ctx %>/participants/participantList.jsp" method="get">
            <button type="submit" name="listButton" class="main-button">参加者一覧に戻る</button>
        </form>

    </section>

</main>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
