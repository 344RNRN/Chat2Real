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
    // JSP を直接開いたときは Action を通して開き直す
    if (request.getAttribute("eventEndLoaded") == null) {
        response.sendRedirect(request.getContextPath() + "/admin/EventEnd.action");
        return;
    }

    // ===== サーブレット（EventEndAction）から受け取るデータ =====
    EventDashboardBean ev = (EventDashboardBean) request.getAttribute("event");
    String accountName = (String) request.getAttribute("accountName");
    String errorMessage = (String) request.getAttribute("errorMessage");
    @SuppressWarnings("unchecked")
    List<EventDashboardBean> nextEvents = (List<EventDashboardBean>) request.getAttribute("nextEvents");
    if (nextEvents == null) nextEvents = new ArrayList<>();

    boolean ended = "終了".equals(ev.getEventStatus());
    String selectedNext = request.getParameter("nextEvent");
    boolean checked = "on".equals(request.getParameter("confirmDelete"));

    String ctx = request.getContextPath();
    String dashboardUrl = ctx + "/admin/EventDashboard.action?eventId=" + ev.getEventId();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>イベント終了 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/event-dashboard.css?v=1">
    <link rel="stylesheet" href="<%= ctx %>/css/event-end.css">
</head>

<body>

    <!-- Common header -->
    <%@ include file="/common/header.jsp" %>

    <main class="page dashboard event-end">

        <!-- 運営メニュー（イベント管理画面と同じ） -->
        <div class="event-bar">
            <div class="event-bar-inner">

                <nav aria-label="運営メニュー">
                    <ul class="event-nav">
                        <li><a href="<%= dashboardUrl %>">ダッシュボード</a></li>
                        <li><a href="#" class="is-active" aria-current="page">イベント終了</a></li>
                    </ul>
                </nav>

                <div class="operator">
                    <span>運営者：<strong><%= h(accountName) %></strong> さん</span>

                    <form action="<%= ctx %>/admin/logout" method="post"
                          onsubmit="return confirm('ログアウトしますか？');">
                        <button type="submit" class="logout-btn">ログアウト</button>
                    </form>
                </div>

            </div>
        </div>

        <div class="container">

            <section class="panel end-panel">

                <p class="end-event-name">
                    <%= h(ev.getEventName()) %>
                    <span class="status-badge"><%= h(ev.getEventStatus()) %></span>
                </p>

<% if (ended) { %>

                <h1 class="end-title">このイベントは終了しています</h1>
                <p class="end-lead">終了レポートは参加者のスマートフォンに表示されています。</p>

                <div class="end-buttons">
                    <button type="button" class="btn" onclick="location.href='<%= dashboardUrl %>'">イベント管理に戻る</button>
                </div>

<% } else { %>

                <!-- 1. 確認メッセージ -->
                <h1 class="end-title">イベントを終了しますか？</h1>

                <!-- 2. 警告文 -->
                <div class="end-warning" role="note">
                    イベントを終了すると、イベント内のチャット・参加者情報は完全に削除されます。<br>
                    この操作は元に戻せません。
                </div>

<% if (errorMessage != null) { %>
                <div class="end-error" role="alert"><%= h(errorMessage) %></div>
<% } %>

                <form action="<%= ctx %>/admin/EventEnd.action" method="post" id="endForm">
                    <input type="hidden" name="eventId" value="<%= ev.getEventId() %>">

                    <!-- 3. 集計サマリー -->
                    <h2 class="end-subtitle">終了時の集計（終了レポートとして参加者に配信されます）</h2>
                    <table class="end-summary">
                        <tr>
                            <th>参加者数</th>
                            <td><%= ev.getParticipantCount() %>名</td>
                        </tr>
                        <tr>
                            <th>直接交流の成立</th>
                            <td><%= ev.getMeetupUserCount() %>人</td>
                        </tr>
                        <tr>
                            <th>満足度（平均）</th>
                            <td><%= h(ev.getSatisfactionText()) %> / 5</td>
                        </tr>
                    </table>

                    <!-- 4. 次回イベントの案内（任意） -->
                    <label for="nextEvent" class="end-subtitle">次回イベントの案内（任意）</label>
                    <select id="nextEvent" name="nextEvent" class="end-select">
                        <option value="">なし</option>
<% for (EventDashboardBean n : nextEvents) {
       String id = String.valueOf(n.getEventId()); %>
                        <option value="<%= id %>"<%= id.equals(selectedNext) ? " selected" : "" %>>
                            <%= h(n.getEventName()) %>（<%= h(n.getDateText()) %>）
                        </option>
<% } %>
                    </select>
                    <p class="end-hint">選ぶと、次回イベントの案内をお知らせとして参加者に配信します。</p>

                    <!-- 5. 確認チェック -->
                    <label class="end-check">
                        <input type="checkbox" name="confirmDelete" id="confirmDelete"<%= checked ? " checked" : "" %>>
                        データが削除されることを確認しました
                    </label>

                    <!-- 6, 7. キャンセル・終了 -->
                    <div class="end-buttons">
                        <button type="button" name="cancelButton" class="btn"
                                onclick="location.href='<%= dashboardUrl %>'">キャンセル</button>
                        <button type="submit" name="closeButton" id="closeButton" class="btn btn-danger"
                                <%= checked ? "" : "disabled" %>>イベントを終了する</button>
                    </div>
                </form>

                <p class="end-note">終了レポートは参加者のスマートフォンに表示されます</p>

<% } %>

            </section>

        </div>

    </main>

<% if (!ended) { %>
    <script>
        // 確認チェックが ON のときだけ「イベントを終了する」を押せる
        const confirmBox = document.getElementById("confirmDelete");
        const closeButton = document.getElementById("closeButton");

        confirmBox.addEventListener("change", function () {
            closeButton.disabled = !confirmBox.checked;
        });

        // 最後にもう一度確認する（元に戻せないため）
        document.getElementById("endForm").addEventListener("submit", function (e) {
            if (!confirm("イベントを終了します。よろしいですか？")) {
                e.preventDefault();
            }
        });
    </script>
<% } %>

    <!-- Common footer (closes </body></html>) -->
    <%@ include file="/common/footer.jsp" %>
