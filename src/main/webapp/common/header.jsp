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
