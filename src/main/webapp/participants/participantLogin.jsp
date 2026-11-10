<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="Bean.ParticipantLoginBean" %>
<%!
    private static String h(Object value) {
        if (value == null) {
            return "";
        }
        String s = value.toString();
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&':  sb.append("&amp;");  break;
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
%>
<%
    String ctx = request.getContextPath();

    // Action を通さずに開いたときは Action から開き直す
    if (request.getAttribute("loginLoaded") == null) {
        response.sendRedirect(ctx + "/participant/Login.action");
        return;
    }

    ParticipantLoginBean login = (ParticipantLoginBean) request.getAttribute("login");   // null = イベントなし
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>参加者ログイン | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/account-register.css">
    <style>
        /* サーバーからのエラー（参加登録画面と同じ） */
        .server-error {
            margin: 0 0 18px;
            padding: 12px 14px;
            color: #c62828;
            background: #fff1f2;
            border: 1px solid #fecdd3;
            border-radius: 10px;
            font-size: 13px;
            font-weight: 700;
            line-height: 1.7;
        }
        .login-switch {
            margin: 18px 0 0;
            color: var(--muted);
            font-size: 13px;
            text-align: center;
        }
        .login-switch a {
            color: var(--pink);
            font-weight: 700;
            text-decoration: underline;
            text-underline-offset: 3px;
        }
    </style>
</head>
<body>
<%@ include file="/common/header-participants.jsp" %>

<main class="page-layout">
    <section class="event-intro">
        <span class="eyebrow">参加者ログイン</span>
    <% if (login != null) { %>
        <h1><%= h(login.getEventName()) %></h1>
        <div class="event-meta">
            <span><%= h(login.getDateText()) %></span>
            <span><%= h(login.getVenue()) %></span>
        </div>
    <% } else { %>
        <h1>ログインできるイベントがありません</h1>
    <% } %>

        <div class="people-illustration">
            <img src="<%= ctx %>/images/people.png"
                 alt="イベントで楽しく会話する男女4人">
        </div>
    </section>

    <section class="form-card">
        <div class="form-heading">
            <span class="heading-icon">♙</span>
            <h2>参加者ログイン</h2>
            <p>参加登録のときに入力したニックネームと本名で、自分のアカウントに戻れます。</p>
        </div>

    <% if (errorMessage != null) { %>
        <div class="server-error" role="alert"><%= h(errorMessage) %></div>
    <% } %>

    <% if (login != null) { %>
        <form id="loginForm" action="<%= ctx %>/participant/Login.action" method="POST">

            <!-- どのイベントにログインするか -->
            <input type="hidden" name="eventId" value="<%= login.getEventId() %>">

            <div class="form-group">
                <label for="nickname" class="required">ニックネーム</label>
                <input type="text" id="nickname" name="nickname" maxlength="20"
                       autocomplete="nickname" placeholder="例：みお"
                       value="<%= h(login.getNickname()) %>">
                <div id="nicknameError" class="error-msg"></div>
            </div>

            <div class="form-group">
                <label for="realName" class="required">本名</label>
                <input type="text" id="realName" name="realName" maxlength="30"
                       autocomplete="name" placeholder="例：山田 友紀"
                       value="<%= h(login.getRealName()) %>">
                <div id="realNameError" class="error-msg"></div>
            </div>

            <div class="info-box">
                参加登録のときと同じニックネーム・本名を入力してください（本名の空白はあってもなくても大丈夫です）。
                本名がほかの参加者に表示されることはありません。
            </div>

            <button type="submit" class="btn-submit">ログイン <span>›</span></button>
        </form>

        <p class="login-switch">
            まだ登録していない方は
            <a href="<%= ctx %>/participant/AccountRegister.action?eventId=<%= login.getEventId() %>">参加登録へ</a>
        </p>
    <% } %>
    </section>
</main>

<script>
(function () {
    const form = document.getElementById('loginForm');
    if (!form) return;   // イベントが無いとき

    form.addEventListener('submit', function (event) {
        let isValid = true;
        document.querySelectorAll('.error-msg').forEach(el => {
            el.style.display = 'none';
            el.textContent = '';
        });

        if (!document.getElementById('nickname').value.trim()) {
            const el = document.getElementById('nicknameError');
            el.textContent = 'ニックネームを入力してください';
            el.style.display = 'block';
            isValid = false;
        }
        if (!document.getElementById('realName').value.trim()) {
            const el = document.getElementById('realNameError');
            el.textContent = '本名を入力してください';
            el.style.display = 'block';
            isValid = false;
        }

        if (!isValid) event.preventDefault();
    });
})();
</script>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
