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
    // ===== サーブレット（MeetResultAction）から受け取るデータ =====
    String partnerName = (String) request.getAttribute("partnerName");     // 相手のニックネーム
    Object exchangeRequestId = request.getAttribute("exchangeRequestId");  // どの交流申請の結果か
    String errorMessage = (String) request.getAttribute("errorMessage");   // 未選択のときのエラー

    // 選んでいた結果（登録済み、またはエラーで戻ったとき）
    String result = request.getParameter("result");
    String reason = request.getParameter("reason");
    if (result == null) result = (String) request.getAttribute("savedResult");
    if (reason == null) reason = (String) request.getAttribute("savedReason");

    if (partnerName == null) partnerName = "";

    // 会えなかった理由（画面設計書 No.4 の選択肢）
    String[] reasons = { "見つけられなかった", "時間がなかった", "その他" };

    boolean isNotMet = "notMet".equals(result);

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>交流結果の登録 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/meet-result.css">
</head>
<body>

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="result-page">

    <!-- 戻るボタン（合流画面へ） -->
<% if (exchangeRequestId != null) { %>
    <a href="<%= ctx %>/participants/meetup?exchangeRequestId=<%= h(exchangeRequestId) %>" class="back-link">‹ 戻る</a>
<% } else { %>
    <a href="javascript:history.back()" class="back-link">‹ 戻る</a>
<% } %>

    <!-- ページ見出し -->
    <section class="page-heading">
        <span class="page-eyebrow">RESULT</span>
        <h1>交流結果の登録</h1>
<% if (!partnerName.isEmpty()) { %>
        <p class="page-description"><%= h(partnerName) %>さん</p>
<% } %>
    </section>

<% if (partnerName.isEmpty()) { %>

    <!-- 相手がいないとき（URLが違う・自分の交流申請ではない） -->
    <section class="result-panel">
        <p class="empty-message">登録する交流の情報がありません。</p>
    </section>

<% } else { %>

<% if (errorMessage != null) { %>
    <div class="error-message" role="alert"><%= h(errorMessage) %></div>
<% } %>

    <form action="<%= ctx %>/participants/meetResult" method="post" class="result-panel">
        <input type="hidden" name="exchangeRequestId" value="<%= h(exchangeRequestId) %>">

        <!-- 1. 質問文 -->
        <h2 class="question"><%= h(partnerName) %>さんと実際に会って<br>話せましたか？</h2>

        <!-- 2, 3. 会って話せた／会えなかった -->
        <div class="choice-list">
            <label class="choice met">
                <input type="radio" name="result" value="met" required<%= "met".equals(result) ? " checked" : "" %>>
                <span>会って話せた</span>
            </label>

            <label class="choice not-met">
                <input type="radio" name="result" value="notMet"<%= isNotMet ? " checked" : "" %>>
                <span>会えなかった</span>
            </label>
        </div>

        <!-- 4. 会えなかった理由（「会えなかった」のときだけ選べる） -->
        <label for="reason" class="reason-label">会えなかった理由（任意）</label>
        <select id="reason" name="reason" class="reason-select"<%= isNotMet ? "" : " disabled" %>>
            <option value="">選択してください</option>
<% for (String r : reasons) { %>
            <option value="<%= r %>"<%= r.equals(reason) ? " selected" : "" %>><%= r %></option>
<% } %>
        </select>

        <!-- 5. 注意書き -->
        <p class="result-note">
            登録した結果は運営者の集計にのみ使われ、相手には表示されません。
        </p>

        <!-- 6. 登録する -->
        <button type="submit" name="registerButton" class="register-button">登録する</button>
    </form>

<% } %>

</main>

<%@ include file="/common/footer.jsp" %>

<script>
    // 「会えなかった」を選んだときだけ、理由を選べるようにする
    const reasonSelect = document.getElementById("reason");

    document.querySelectorAll('input[name="result"]').forEach(function (radio) {
        radio.addEventListener("change", function () {
            const notMet = radio.value === "notMet" && radio.checked;
            reasonSelect.disabled = !notMet;
            if (!notMet) {
                reasonSelect.value = "";
            }
        });
    });
</script>

</body>
</html>
