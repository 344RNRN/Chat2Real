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

    // ===== サーブレット（AccountProfileAction）から受け取るデータ =====
    AccountProfileBean profile = (AccountProfileBean) request.getAttribute("profile");

    // Action を通さずに開いたときは Action から開き直す
    if (profile == null) {
        response.sendRedirect(ctx + "/participant/AccountProfile.action");
        return;
    }

    boolean isMine = Boolean.TRUE.equals(request.getAttribute("isMine"));     // 自分のプロフィールか
    boolean canChat = Boolean.TRUE.equals(request.getAttribute("canChat"));   // チャットできるか
    String chatDisabledReason = (String) request.getAttribute("chatDisabledReason");
    String chatUrl = (String) request.getAttribute("chatUrl");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isMine ? "プロフィール" : h(profile.getNickname()) + "さんのプロフィール" %> | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/account-profile.css">
</head>
<body class="account-page">

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="account-main">

    <!-- ページ見出し -->
    <section class="page-heading">
    <% if (isMine) { %>
        <span class="eyebrow">マイプロフィール</span>
        <h1 class="page-title">プロフィール</h1>
    <% } else { %>
        <span class="eyebrow">参加者プロフィール</span>
        <h1 class="page-title"><%= h(profile.getNickname()) %> さん</h1>
    <% } %>
        <p class="page-description"><%= h(profile.getEventName()) %></p>
    </section>

    <!-- 1. 写真・ニックネーム・年代・状態 -->
    <section class="card profile-main">
        <span class="avatar large">
        <% if (!isBlank(profile.getProfileImageUrl())) { %>
            <img src="<%= ctx %><%= h(profile.getProfileImageUrl()) %>" alt="<%= h(profile.getNickname()) %>さんの写真">
        <% } else { %>
            <span aria-hidden="true"><%= h(profile.getInitial()) %></span>
        <% } %>
        </span>

        <h2 class="profile-name"><%= h(profile.getNickname()) %></h2>
        <p class="profile-age"><%= h(profile.getAgeGroup()) %></p>

        <span class="status-badge <%= profile.isWantToMeet() ? "want" : "not-now" %>">
            <%= profile.isWantToMeet() ? "交流したい" : "今は交流したくない" %>
        </span>
    </section>

    <!-- 2. ほかの参加者に公開される情報 -->
    <section class="card">
        <div class="section-title-bar">
        <% if (isMine) { %>
            ほかの参加者に公開される情報
            <span class="visibility public">公開</span>
        <% } else { %>
            自己紹介
        <% } %>
        </div>

        <dl class="profile-list">
            <dt>一言・参加目的</dt>
            <dd>
            <% if (isBlank(profile.getSelfIntroduction())) { %>
                <span class="muted">未入力</span>
            <% } else { %>
                <%= h(profile.getSelfIntroduction()) %>
            <% } %>
            </dd>

            <dt>興味・特徴</dt>
            <dd>
            <% if (profile.getInterests().isEmpty()) { %>
                <span class="muted">未登録</span>
            <% } else { %>
                <div class="tag-list">
                <% for (String tag : profile.getInterests()) { %>
                    <span class="tag"><%= h(tag) %></span>
                <% } %>
                </div>
            <% } %>
            </dd>
        </dl>

    <% if (isMine) { %>
        <div class="info-box">
        <% if (profile.isWantToMeet()) { %>
            「交流したい」を選んでいるので、参加者一覧に表示されています。
        <% } else { %>
            「今は交流したくない」を選んでいるので、参加者一覧には表示されていません。
        <% } %>
        </div>
    <% } %>
    </section>

<% if (isMine) { %>
    <!-- 3. 運営者のみ閲覧できる情報（本人にだけ表示する） -->
    <section class="card">
        <div class="section-title-bar">
            運営者のみ閲覧できる情報
            <span class="visibility private">非公開</span>
        </div>

        <dl class="profile-list">
            <dt>本名</dt>
            <dd><%= h(profile.getRealName()) %></dd>

            <dt>プロフィール写真</dt>
            <dd><%= isBlank(profile.getProfileImageUrl()) ? "未登録" : "登録済み" %></dd>
        </dl>
    </section>
<% } %>

    <!-- 4. 参加しているイベント -->
    <section class="card">
        <div class="section-title-bar">参加しているイベント</div>

        <dl class="profile-list">
            <dt>イベント名</dt>
            <dd><%= h(profile.getEventName()) %></dd>

            <dt>日時</dt>
            <dd><%= h(profile.getDateText()) %></dd>

            <dt>会場</dt>
            <dd><%= h(profile.getVenue()) %></dd>
        </dl>
    </section>

    <!-- 5. チャットする・イベントルームへ -->
    <div class="button-row">
    <% if (canChat) { %>
        <!-- ほかの参加者：その人と1対1でチャット -->
        <a href="<%= h(chatUrl) %>" class="btn-primary">
            <span class="btn-icon" aria-hidden="true">💬</span> <%= h(profile.getNickname()) %> さんとチャットする
        </a>
    <% } else { %>
        <!-- 自分のプロフィール・相手が交流を希望していないとき：押せない -->
        <button type="button" class="btn-primary" disabled aria-describedby="chatDisabledReason">
            <span class="btn-icon" aria-hidden="true">💬</span> チャットする
        </button>
        <p class="disabled-reason" id="chatDisabledReason"><%= h(chatDisabledReason) %></p>
    <% } %>

    <% if (isMine) { %>
        <a href="<%= ctx %>/admin/topScreen.jsp" class="btn-secondary">イベントルームへ進む</a>
    <% } else { %>
        <a href="<%= ctx %>/participant/ParticipantList.action" class="btn-secondary">← 参加者一覧に戻る</a>
        <a href="<%= ctx %>/participant/AccountProfile.action" class="text-link">自分のプロフィールを見る</a>
    <% } %>

        <p class="footer-note">
            今回のプロフィール・チャットは、イベント終了日に削除されます（次回のイベントには引き継がれません）。
        </p>
    </div>

</main>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
