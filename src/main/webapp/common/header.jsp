<%@ page pageEncoding="UTF-8" %>
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
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Chat2Real 運営</title>
<style>
    * { box-sizing: border-box; }
    body {
        margin: 0;
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        background: #F7F4EF;
        color: #211F1C;
        font-family: "Meiryo UI", Meiryo, "Hiragino Sans", sans-serif;
        font-size: 14px;
    }

    /* ---------- ヘッダー ---------- */
    .site-header {
        display: flex;
        align-items: center;
        gap: 24px;
        height: 56px;
        padding: 0 24px;
        background: #211F1C;
        color: #FFFFFF;
    }
    .site-logo {
        margin: 0;
        font-size: 18px;
        font-weight: bold;
    }
    .site-logo a { color: #FFFFFF; text-decoration: none; }
    .header-event { margin: 0; color: #E9E6E0; }
    .header-menu {
        margin-left: auto;
        display: flex;
        align-items: center;
        gap: 16px;
        color: #E9E6E0;
    }
    .btn-logout {
        width: 90px;
        height: 40px;
        border: 1px solid #8B8579;
        border-radius: 4px;
        background: #3A3631;
        color: #FFFFFF;
        cursor: pointer;
    }

    /* ---------- 画面の中身（共通） ---------- */
    .page {
        flex: 1;
        display: flex;
        justify-content: center;
        align-items: flex-start;
        padding: 48px 16px;
    }
    .card {
        width: 100%;
        max-width: 420px;
        padding: 32px 40px;
        background: #FFFFFF;
        border: 1px solid #BFB8AC;
        border-radius: 6px;
    }
    .card-title {
        margin: 0 0 8px;
        font-size: 22px;
        text-align: center;
    }
    .card-lead {
        margin: 0 0 24px;
        color: #8B8579;
        font-size: 12px;
        text-align: center;
    }
    .form-item { margin-bottom: 16px; }
    .form-item label {
        display: block;
        margin-bottom: 6px;
        color: #55504A;
        font-weight: bold;
        font-size: 13px;
    }
    .required { color: #C0392B; }
    .form-item input {
        width: 100%;
        max-width: 320px;
        height: 36px;
        padding: 0 10px;
        border: 1px solid #BFB8AC;
        border-radius: 4px;
        font-size: 14px;
    }
    .form-item input:focus {
        outline: 2px solid #E8873A;
        border-color: #E8873A;
    }
    .error-message {
        margin: 0 0 16px;
        color: #C0392B;
        font-weight: bold;
        font-size: 13px;
    }
    .btn-primary {
        width: 100%;
        max-width: 320px;
        height: 40px;
        border: none;
        border-radius: 4px;
        background: #E8873A;
        color: #FFFFFF;
        font-size: 15px;
        font-weight: bold;
        cursor: pointer;
    }
    .btn-primary:hover { background: #D67427; }
    .card-links {
        margin: 20px 0 0;
        text-align: center;
        font-size: 13px;
    }
    .card-links a { color: #7A3E10; }
    .card-note {
        margin: 16px 0 0;
        color: #8B8579;
        font-size: 12px;
        text-align: center;
    }

    /* ---------- フッター ---------- */
    .site-footer {
        padding: 12px 0;
        border-top: 1px solid #BFB8AC;
        background: #FFFFFF;
        color: #8B8579;
        text-align: center;
    }
</style>
</head>
<body>
<header class="site-header">
    <h1 class="site-logo"><a href="<%= request.getContextPath() %>/EventDashboard.action">Chat2Real 運営</a></h1>
<% if (session.getAttribute("organizerName") != null) { %>
    <p class="header-event"><%= h(session.getAttribute("eventName")) %></p>
    <div class="header-menu">
        <span>運営者：<%= h(session.getAttribute("organizerName")) %> さん</span>
        <button type="button" id="logoutButton" name="logoutButton" class="btn-logout">ログアウト</button>
    </div>
<% } %>
</header>
