<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="Bean.AccountProfileBean" %>
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

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
%>
<%
    String ctx = request.getContextPath();

    // ===== サーブレット（AccountCompleteAction）から受け取るデータ =====
    AccountProfileBean profile = (AccountProfileBean) request.getAttribute("profile");

    // Action を通さずに開いたときは Action から開き直す
    if (profile == null) {
        response.sendRedirect(ctx + "/participant/AccountComplete.action");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登録完了 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/account-profile.css">
</head>
<body class="account-page">

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="account-main">

    <!-- ページ見出し -->
    <section class="page-heading">
        <span class="eyebrow">イベント参加登録</span>
        <h1 class="page-title">登録完了</h1>
        <p class="page-description"><%= h(profile.getEventName()) %></p>
    </section>

    <section class="card complete-card">

        <!-- 1. 完了アイコン -->
        <div class="done-icon" aria-hidden="true">✓</div>

        <!-- 2. 完了メッセージ -->
        <h2 class="complete-title">参加登録が完了しました</h2>

        <!-- 3. 登録したニックネーム -->
        <div class="person-chip">
            <span class="avatar small">
            <% if (!isBlank(profile.getProfileImageUrl())) { %>
                <img src="<%= ctx %><%= h(profile.getProfileImageUrl()) %>" alt="">
            <% } else { %>
                <%= h(profile.getInitial()) %>
            <% } %>
            </span>
            <%= h(profile.getNickname()) %> さん
        </div>

        <!-- 4. イベントの日時・会場 -->
        <p class="event-line">
            <span><%= h(profile.getDateText()) %></span>
            <span><%= h(profile.getVenue()) %></span>
        </p>
        <p class="complete-text">
            気になる参加者とチャットして、会場での交流を楽しみましょう。
        </p>

        <!-- 5. 案内 -->
        <div class="info-box">
            写真・本名は運営者のみ閲覧できます。「交流したい」参加者には、ニックネーム・興味・一言のみ公開されます。
        </div>

        <!-- 6. ボタン -->
        <div class="button-row">
            <a href="<%= ctx %>/participant/AccountProfile.action" class="btn-primary">
                プロフィールを確認する <span class="btn-icon">›</span>
            </a>
            <a href="<%= ctx %>/admin/topScreen.jsp" class="btn-secondary">イベントルームへ進む</a>
        </div>

    </section>

</main>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
