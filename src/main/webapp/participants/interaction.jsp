<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="Bean.InteractionBean" %>
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
    String ctx = request.getContextPath();

    // ===== サーブレット（InteractionAction）から受け取るデータ =====
    InteractionBean it = (InteractionBean) request.getAttribute("interaction");   // null：相手が分からない
    String flash = (String) request.getAttribute("flash");

    String chatUrl = (it == null) ? ctx + "/participant/ParticipantList.action"
                                  : ctx + "/chat?partnerId=" + it.getPartnerId();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>交流申請 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/participant-message.css?v=3">
</head>
<body>

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="message-page">

    <!-- ページ見出し -->
    <section class="page-heading">
        <span class="page-eyebrow">REQUEST</span>
        <h1 class="page-title">交流申請</h1>
    </section>

<% if (flash != null) { %>
    <p class="flash-message" role="status"><%= h(flash) %></p>
<% } %>

    <section class="message-card">

<% if (it == null) { %>

        <p class="empty-message">交流申請する相手の情報がありません。</p>
        <a href="<%= ctx %>/participant/ParticipantList.action" class="sub-link">参加者一覧へ</a>

<% } else { %>

        <!-- 相手 -->
        <div class="pair">
            <span class="avatar large partner">
<% if (it.getPartnerPhoto() != null && !it.getPartnerPhoto().isEmpty()) { %>
                <img src="<%= ctx %><%= h(it.getPartnerPhoto()) %>" alt="<%= h(it.getPartnerName()) %>さんの写真">
<% } else { %>
                <%= initial(it.getPartnerName()) %>
<% } %>
            </span>
        </div>

    <% if (it.isIncoming()) { %>

        <!-- 相手から申請が届いている -->
        <span class="state-badge incoming">申請が届いています</span>
        <h2 class="message-title"><%= h(it.getPartnerName()) %>さんが<br>会場で直接話したいと思っています</h2>
        <p class="message-text">
            あなたも会いたいときは「直接交流する」を押してください。<br>
            お互いが承認すると、合流の画面に進めます。
        </p>

        <form action="<%= ctx %>/interaction/request" method="post" class="button-row">
            <input type="hidden" name="partnerId" value="<%= it.getPartnerId() %>">
            <button type="submit" name="op" value="approve" class="main-button">直接交流する</button>
            <button type="submit" name="op" value="decline" class="outline-button"
                    onclick="return confirm('今回は見送りますか？\n相手にはやさしいお知らせだけが届き、チャットはこのまま続けられます。');">今はやめておく</button>
        </form>

    <% } else if (it.isWaiting()) { %>

        <!-- 自分が申請して、返事待ち -->
        <span class="state-badge waiting">返事を待っています</span>
        <h2 class="message-title"><%= h(it.getPartnerName()) %>さんに<br>交流申請を送りました</h2>
        <p class="message-text">
            <%= h(it.getPartnerName()) %>さんが承認すると、自動で次の画面に進みます。<br>
            そのあいだもチャットは続けられます。
        </p>
        <p class="waiting-dots" aria-hidden="true"><span></span><span></span><span></span></p>

        <form action="<%= ctx %>/interaction/request" method="post">
            <input type="hidden" name="partnerId" value="<%= it.getPartnerId() %>">
            <button type="submit" name="op" value="cancel" class="outline-button"
                    onclick="return confirm('交流申請を取り消しますか？');">申請を取り消す</button>
        </form>

    <% } else if (it.isDeclinedForMe()) { %>

        <!-- 自分の申請が見送られた：相手を責めない、やさしいお知らせ -->
        <span class="state-badge soft">お知らせ</span>
        <h2 class="message-title">今回はタイミングが<br>合わなかったようです</h2>
        <p class="message-text">
            <%= h(it.getPartnerName()) %>さんは、今は直接の交流を見送りました。<br>
            忙しかったり、ほかの予定があったりと、理由はいろいろです。<br>
            気にせず、イベントを楽しんでくださいね。
        </p>
        <p class="note soft-note">
            🌱 チャットはこのまま続けられます。<br>
            ほかの参加者とも、気軽に話してみましょう。
        </p>

        <div class="button-row">
            <a href="<%= chatUrl %>" class="main-button">チャットを続ける</a>
            <a href="<%= ctx %>/participant/ParticipantList.action" class="outline-button">ほかの参加者を見る</a>
        </div>

        <form action="<%= ctx %>/interaction/request" method="post" class="again-form">
            <input type="hidden" name="partnerId" value="<%= it.getPartnerId() %>">
            <button type="submit" name="op" value="request" class="text-button"
                    onclick="return confirm('もう一度、交流申請を送りますか？');">しばらくしてから、もう一度申請する</button>
        </form>

    <% } else { %>

        <!-- まだ申請していない（または取り消し済み） -->
        <h2 class="message-title"><%= h(it.getPartnerName()) %>さんと<br>会場で直接話してみませんか？</h2>
        <p class="message-text">
            交流申請を送ると、<%= h(it.getPartnerName()) %>さんに届きます。<br>
            お互いが「直接交流したい」となったら、合流して会場で話せます。
        </p>

        <form action="<%= ctx %>/interaction/request" method="post">
            <input type="hidden" name="partnerId" value="<%= it.getPartnerId() %>">
            <button type="submit" name="op" value="request" class="main-button">交流申請を送る</button>
        </form>

        <p class="note">
            申請はいつでも取り消せます。相手が断っても、相手には理由は伝わりません。
        </p>

    <% } %>

    <% if (!it.isDeclinedForMe()) { %>
        <!-- チャットに戻る -->
        <a href="<%= chatUrl %>" class="sub-link">‹ チャットに戻る</a>
    <% } %>

<% } %>

    </section>

</main>

<% if (it != null && it.isWaiting()) { %>
<script>
    // 返事待ちのあいだ、5秒ごとに相手が承認したか確認する → 承認されたら相互承認画面へ
    (function () {
        var url = "<%= ctx %>/interaction/request?partnerId=<%= it.getPartnerId() %>&format=json";
        setInterval(function () {
            if (document.hidden) return;
            fetch(url, { cache: "no-store", credentials: "same-origin" })
                .then(function (r) { return r.ok ? r.json() : null; })
                .then(function (s) {
                    if (!s) return;
                    if (s.approved || !s.waiting) location.reload();   // 承認・辞退されたら画面を更新
                })
                .catch(function () {});
        }, 5000);
    })();
</script>
<% } %>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
