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

    // 写真が無いときは名前の1文字目
    private static String initial(String name) {
        return (name == null || name.isEmpty()) ? "?" : h(name.substring(0, 1));
    }
%>
<%
    // ===== サーブレット（MutualConsentAction）から受け取るデータ =====
    Boolean approved = (Boolean) request.getAttribute("approved");           // null：自分の交流申請ではない
    Object exchangeRequestId = request.getAttribute("exchangeRequestId");
    String partnerName = (String) request.getAttribute("partnerName");
    String partnerPhoto = (String) request.getAttribute("partnerPhoto");
    String myName = (String) request.getAttribute("myName");
    String myPhoto = (String) request.getAttribute("myPhoto");

    if (partnerName == null) partnerName = "";

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>相互承認 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/participant-message.css?v=2">
</head>
<body>

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="message-page">

    <!-- ページ見出し -->
    <section class="page-heading">
        <span class="page-eyebrow">MATCH</span>
        <h1 class="page-title">相互承認</h1>
    </section>

    <section class="message-card">

<% if (approved == null) { %>

        <p class="empty-message">交流申請の情報がありません。</p>

<% } else if (!approved) { %>

        <h2 class="message-title">まだ相互承認されていません</h2>
        <p class="message-text">
            お互いが「直接交流したい」を選んだときに表示されます。
        </p>

<% } else { %>

        <!-- 1, 2. 自分・相手のアイコンと成立アイコン -->
        <div class="pair">
            <span class="avatar large me">
<% if (myPhoto != null) { %>
                <img src="<%= ctx %><%= h(myPhoto) %>" alt="あなたの写真">
<% } else { %>
                <%= initial(myName) %>
<% } %>
            </span>

            <span class="status-icon match" aria-hidden="true">♥</span>

            <span class="avatar large partner">
<% if (partnerPhoto != null) { %>
                <img src="<%= ctx %><%= h(partnerPhoto) %>" alt="<%= h(partnerName) %>さんの写真">
<% } else { %>
                <%= initial(partnerName) %>
<% } %>
            </span>
        </div>

        <!-- 3. 成立メッセージ -->
        <h2 class="message-title">相互承認が成立しました！</h2>

        <!-- 4. 説明文 -->
        <p class="message-text">
            <%= h(partnerName) %>さんも、あなたと会場で直接話したいと思っています。<br>
            合図を送って、合流しましょう。
        </p>

        <!-- 5. 注意書き -->
        <p class="note">
            合流するときは、周りの人に気をつけて、人の多い明るい場所で会いましょう。
        </p>

        <!-- 6. 合図を送る（合流画面へ） -->
        <form action="<%= ctx %>/participants/meetup" method="get">
            <input type="hidden" name="exchangeRequestId" value="<%= h(exchangeRequestId) %>">
            <button type="submit" name="signalButton" class="main-button">合図を送る</button>
        </form>

<% } %>

        <!-- 7. チャットに戻る -->
        <a href="javascript:history.back()" class="sub-link">チャットに戻る</a>

    </section>

</main>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
