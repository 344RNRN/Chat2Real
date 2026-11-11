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
    // ===== サーブレットから受け取るデータ（まだ無いときは空にする） =====
    String partnerName = (String) request.getAttribute("partnerName");     // 相手のニックネーム（例：遥）
    String partnerReading = (String) request.getAttribute("partnerReading"); // 読み方（例：はるか）※無くてもよい
    String partnerPhoto = (String) request.getAttribute("partnerPhoto");   // 相手の写真のURL（無いときは null）
    String partnerZone = (String) request.getAttribute("partnerZone");     // 相手が送ったエリア（まだ無いときは null）
    String myZone = (String) request.getAttribute("myZone");               // 自分が送ったエリア（まだ無いときは null）
    Object exchangeRequestId = request.getAttribute("exchangeRequestId");  // どの交流申請の合流か

    if (partnerName == null) partnerName = "";

    // 合流エリア・会場マップ（運営者がお知らせ配信画面で設定。無いときは初期の4エリア）
    @SuppressWarnings("unchecked")
    java.util.List<String> zones = (java.util.List<String>) request.getAttribute("zones");
    if (zones == null) zones = java.util.List.of("入口付近", "ステージ前", "中央テーブル", "ドリンクコーナー");
    String venueMapPath = (String) request.getAttribute("venueMapPath");

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>合流 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/meetup.css?v=3">
</head>
<body>

<%-- 参加者用ヘッダー（参加者一覧と同じ） --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="meetup-page">

    <!-- 戻るボタン（共通部） -->
    <a href="javascript:history.back()" class="back-link">‹ 戻る</a>

    <!-- ページ見出し -->
    <section class="page-heading">
        <span class="page-eyebrow">MEETUP</span>
        <h1>合流</h1>
<% if (partnerName.isEmpty()) { %>
        <p class="page-description">合流する相手の情報がありません。</p>
<% } else { %>
        <p class="page-description"><%= h(partnerName) %>さんを探しましょう。</p>
<% } %>
    </section>

    <!-- 1～3. 相手の写真・ニックネーム・現在エリア -->
    <section class="partner-card">
        <div class="partner-photo">
<% if (partnerPhoto != null) { %>
            <img src="<%= ctx %><%= h(partnerPhoto) %>" alt="<%= h(partnerName) %>さんの写真">
<% } else { %>
            <span><%= partnerName.isEmpty() ? "?" : h(partnerName.substring(0, 1)) %></span>
<% } %>
        </div>

        <div class="partner-info">
            <p class="partner-name">
                <%= partnerName.isEmpty() ? "－" : h(partnerName) %>
<% if (partnerReading != null) { %>
                <span class="reading">（<%= h(partnerReading) %>）</span>
<% } %>
            </p>
            <p class="partner-zone-label">相手の現在エリア</p>
<% if (partnerZone != null) { %>
            <span class="zone-badge partner"><%= h(partnerZone) %></span>
<% } else { %>
            <span class="zone-badge none">まだ合図がありません</span>
<% } %>
        </div>
    </section>

    <!-- 4, 5. 自分のエリアを選んで合図を送る -->
    <section class="signal-panel">
        <form action="<%= ctx %>/participants/meetupSignal" method="post" class="signal-form">
            <input type="hidden" name="exchangeRequestId" value="<%= h(exchangeRequestId) %>">

            <label for="zone" class="section-title">あなたの現在エリア</label>
            <select id="zone" name="zone" class="zone-select" required>
                <option value="">選択してください</option>
<% for (String zone : zones) { %>
                <option value="<%= h(zone) %>"<%= zone.equals(myZone) ? " selected" : "" %>><%= h(zone) %></option>
<% } %>
            </select>

            <button type="submit" name="signalButton" class="signal-button">合図を送る</button>
        </form>

<% if (myZone != null) { %>
        <p class="signal-sent">「<%= h(myZone) %>」で合図を送りました。</p>
<% } %>
    </section>

    <!-- 6. 会場マップ（相手・自分のエリアに印を表示） -->
    <section class="map-panel">
        <p class="section-title">会場マップ</p>
<% if (venueMapPath != null) { %>
        <!-- 運営者が入れた会場の写真（押すと大きく表示） -->
        <a class="venue-photo" href="<%= ctx %><%= h(venueMapPath) %>" target="_blank" rel="noopener">
            <img src="<%= ctx %><%= h(venueMapPath) %>" alt="会場マップ">
            <span class="venue-photo-zoom">タップで拡大 ↗</span>
        </a>
<% } %>
        <div class="venue-map">
<% for (String zone : zones) {
       boolean isPartner = zone.equals(partnerZone);
       boolean isMine = zone.equals(myZone); %>
            <div class="map-zone<%= isPartner ? " has-partner" : "" %><%= isMine ? " has-me" : "" %>">
                <span class="map-zone-name"><%= h(zone) %></span>
<% if (isPartner) { %>
                <span class="map-pin partner"><%= h(partnerName) %>さん</span>
<% } %>
<% if (isMine) { %>
                <span class="map-pin me">あなた</span>
<% } %>
            </div>
<% } %>
        </div>
    </section>

    <!-- 7. 注意書き -->
    <p class="meetup-note">
        エリアは合流する相手にだけ表示されます。会場では周りの人に気をつけて、安全に合流してください。合流できたら、交流結果を登録してください。
    </p>

    <!-- 8. 交流結果を登録する -->
    <form action="<%= ctx %>/participants/meetResult" method="get">
        <input type="hidden" name="exchangeRequestId" value="<%= h(exchangeRequestId) %>">
        <button type="submit" name="resultButton" class="result-button">交流結果を登録する</button>
    </form>

</main>

<script>
    // 相手が合図を送ると表示が変わるように、15秒ごとに「相手のエリア」と「会場マップ」だけ取り直す
    // （画面全体の再読み込みはしない。エリアを選んでいる途中もそのまま）
    (function () {
        function swap(selector, doc) {
            var now = document.querySelector(selector);
            var next = doc.querySelector(selector);
            if (now && next && now.innerHTML !== next.innerHTML) {
                now.replaceWith(next);
            }
        }
        setInterval(function () {
            if (document.hidden) return;
            fetch(location.href, { cache: "no-store", credentials: "same-origin" })
                .then(function (r) {
                    if (r.redirected && /EventClosed/.test(r.url)) {   // イベントが終わっていたら終了画面へ
                        location.href = r.url;
                        return null;
                    }
                    return r.ok ? r.text() : null;
                })
                .then(function (html) {
                    if (!html) return;
                    var doc = new DOMParser().parseFromString(html, "text/html");
                    swap(".partner-card", doc);
                    swap(".map-panel", doc);
                })
                .catch(function () {});
        }, 15000);
    })();
</script>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
