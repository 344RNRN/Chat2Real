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

    // 合流エリア（画面設計書 No.4 の選択肢）
    String[] zones = { "入口付近", "ステージ前", "中央テーブル", "ドリンクコーナー" };

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>合流 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/meetup.css">
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
            <img src="<%= h(partnerPhoto) %>" alt="<%= h(partnerName) %>さんの写真">
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
                <option value="<%= zone %>"<%= zone.equals(myZone) ? " selected" : "" %>><%= zone %></option>
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
        <div class="venue-map">
<% for (String zone : zones) {
       boolean isPartner = zone.equals(partnerZone);
       boolean isMine = zone.equals(myZone); %>
            <div class="map-zone<%= isPartner ? " has-partner" : "" %><%= isMine ? " has-me" : "" %>">
                <span class="map-zone-name"><%= zone %></span>
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

<%@ include file="/common/footer.jsp" %>

<script>
    // 相手が合図を送ると表示が変わるように、15秒ごとに画面を更新する
    // （エリアを選んでいる途中は更新しない）
    const zoneSelect = document.getElementById("zone");
    const firstZone = zoneSelect.value;

    setInterval(function () {
        if (zoneSelect.value === firstZone) {
            location.reload();
        }
    }, 15000);
</script>

</body>
</html>
