<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="Bean.EventDashboardBean" %>
<%@ page import="java.util.List, java.util.ArrayList" %>
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
    // JSP を直接開いたとき（ログイン直後など）は Action を通して開き直す
    if (request.getAttribute("dashboardLoaded") == null) {
        response.sendRedirect(request.getContextPath() + "/admin/EventDashboard.action");
        return;
    }

    EventDashboardBean ev = (EventDashboardBean) request.getAttribute("event");
    String accountName = (String) request.getAttribute("accountName");
    @SuppressWarnings("unchecked")
    List<EventDashboardBean> eventList = (List<EventDashboardBean>) request.getAttribute("eventList");
    if (eventList == null) eventList = new ArrayList<>();
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>イベントダッシュボード | Chat2Real</title>

    <link rel="stylesheet"
          href="<%= ctx %>/css/event-dashboard.css?v=1">

    <style>
        /* イベント一覧（クリックでそのイベントを表示） */
        .event-list-panel {
            margin-top: 20px;
            padding: 20px 24px;
        }
        .event-list-panel h3 {
            margin: 0 0 12px;
            color: #173b63;
            font-size: 17px;
        }
        .event-list {
            list-style: none;
            margin: 0;
            padding: 0;
        }
        .event-list a {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            padding: 12px 14px;
            margin-bottom: 8px;
            color: #233b59;
            background: #ffffff;
            border: 1px solid #dce5f1;
            border-radius: 11px;
            text-decoration: none;
            transition: border-color 0.15s ease, background 0.15s ease;
        }
        .event-list a:hover {
            border-color: #f9b6cd;
            background: #fff7fa;
        }
        .event-list a.is-current {
            border-color: #f45b91;
            background: #fff0f6;
        }
        .event-list .list-name {
            font-weight: 700;
            overflow-wrap: anywhere;
        }
        .event-list .list-meta {
            margin-top: 2px;
            color: #7184a0;
            font-size: 13px;
        }
        .event-list .list-status {
            flex-shrink: 0;
            padding: 4px 10px;
            color: #f45b91;
            background: #fff0f6;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
        }
        .event-list .list-empty {
            color: #7184a0;
            font-size: 14px;
        }
    </style>
</head>

<body>

    <!-- Common header -->
    <%@ include file="/common/header.jsp" %>

    <main class="page dashboard">

        <!-- Page navigation (dashboard only) -->
        <div class="event-bar">
            <div class="event-bar-inner">

                <nav aria-label="運営メニュー">
                    <ul class="event-nav">
                        <li><a href="#" class="is-active" aria-current="page">ダッシュボード</a></li>
                    </ul>
                </nav>

                <div class="operator">
                    <span>運営者：<strong><%= h(accountName) %></strong> さん</span>

                    <form action="<%= ctx %>/admin/logout" method="post">
                        <button type="submit" class="logout-btn">ログアウト</button>
                    </form>
                </div>

            </div>
        </div>


        <div class="container">

            <!-- Event info & actions -->
            <section class="panel event-head">

                <div class="event-info">
                <% if (ev != null) { %>
                    <h2>
                        <%= h(ev.getEventName()) %>
                        <span class="status-badge"><%= h(ev.getEventStatus()) %></span>
                    </h2>
                    <p><%= h(ev.getDateText()) %>　<%= h(ev.getVenue()) %></p>
                <% } else { %>
                    <h2>イベントがまだありません</h2>
                    <p>「＋ イベント作成」から最初のイベントを作成してください。</p>
                <% } %>
                </div>

                <div class="action-buttons">
                    <button type="button" class="btn" onclick="location.href='<%= ctx %>/admin/EventCreate.action'">＋ イベント作成</button>
                <% if (ev != null) { %>
                    <button type="button" class="btn" onclick="location.href='<%= ctx %>/admin/EventEdit.action?eventId=<%= ev.getEventId() %>'">編集</button>
                    <button type="button" class="btn" onclick="location.href='<%= ctx %>/admin/SurveyResult.action?eventId=<%= ev.getEventId() %>'">アンケート結果</button>
                    <button type="button" class="btn" onclick="location.href='<%= ctx %>/admin/QRCode.jsp'">QRコード</button>
                    <button type="button" class="btn btn-primary" onclick="location.href='<%= ctx %>/admin/Notification.jsp'">お知らせ配信</button>
                    <button type="button" class="btn btn-danger" onclick="location.href='<%= ctx %>/admin/EventEnd.action?eventId=<%= ev.getEventId() %>'">イベント終了</button>
                <% } %>
                </div>

            </section>


        <% if (ev != null) {
               boolean hasParticipants = ev.getParticipantCount() > 0;
               boolean hasRating = !"－".equals(ev.getSatisfactionText()); %>
            <!-- KPI cards（表示中のイベントの実際の数字） -->
            <section class="kpi-row" aria-label="イベント指標">
                <div class="panel kpi-card">
                    <span class="kpi-title">参加者数</span>
                    <span class="kpi-value"><%= ev.getParticipantCount() %><span class="kpi-unit">名</span></span>
                </div>
                <div class="panel kpi-card">
                    <span class="kpi-title">チャット利用率</span>
                    <span class="kpi-value"><%= ev.getChatRateText() %><% if (hasParticipants) { %><span class="kpi-unit">%</span><% } %></span>
                </div>
                <div class="panel kpi-card">
                    <span class="kpi-title">相互承認率</span>
                    <span class="kpi-value"><%= ev.getApprovedRateText() %><% if (hasParticipants) { %><span class="kpi-unit">%</span><% } %></span>
                </div>
                <div class="panel kpi-card">
                    <span class="kpi-title">直接交流成立率</span>
                    <span class="kpi-value"><%= ev.getMeetupRateText() %><% if (hasParticipants) { %><span class="kpi-unit">%</span><% } %></span>
                </div>
                <div class="panel kpi-card">
                    <span class="kpi-title">満足度（平均）</span>
                    <span class="kpi-value"><%= ev.getSatisfactionText() %><% if (hasRating) { %><span class="kpi-unit">/ 5</span><% } %></span>
                </div>
            </section>


            <!-- Alert（まだ誰とも直接交流できていない参加者がいるときだけ） -->
            <% if (ev.getNoMeetupCount() > 0) { %>
            <div class="alert-banner" role="status">
                <span class="alert-icon">⚠</span>
                <span>まだ誰とも交流できていない参加者：<strong><%= ev.getNoMeetupCount() %>名</strong></span>
                <span>➔「お知らせ配信」で交流タイムを案内しましょう</span>
            </div>
            <% } %>
        <% } %>


            <!-- イベント一覧（クリックするとそのイベントをダッシュボードに表示） -->
            <section class="panel event-list-panel" aria-label="イベント一覧">
                <h3>イベント一覧</h3>
            <% if (eventList.isEmpty()) { %>
                <p class="list-empty">まだイベントがありません。</p>
            <% } else { %>
                <ul class="event-list">
                <% for (EventDashboardBean item : eventList) {
                       boolean current = (ev != null && ev.getEventId() == item.getEventId()); %>
                    <li>
                        <a href="<%= ctx %>/admin/EventDashboard.action?eventId=<%= item.getEventId() %>"
                           class="<%= current ? "is-current" : "" %>"
                           <%= current ? "aria-current=\"page\"" : "" %>>
                            <span>
                                <span class="list-name"><%= h(item.getEventName()) %></span><br>
                                <span class="list-meta"><%= h(item.getDateText()) %>　<%= h(item.getVenue()) %></span>
                            </span>
                            <span class="list-status"><%= h(item.getEventStatus()) %></span>
                        </a>
                    </li>
                <% } %>
                </ul>
            <% } %>
            </section>

        </div>

    </main>

    <!-- Common footer (closes </body></html>) -->
    <%@ include file="/common/footer.jsp" %>
