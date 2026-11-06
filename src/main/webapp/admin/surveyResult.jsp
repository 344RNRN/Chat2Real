<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="Bean.SurveyResultBean" %>
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
    String ctx = request.getContextPath();

    // Action を通さずに JSP を開いたときは Action から開き直す
    if (request.getAttribute("resultLoaded") == null) {
        response.sendRedirect(ctx + "/admin/SurveyResult.action");
        return;
    }

    SurveyResultBean r = (SurveyResultBean) request.getAttribute("result");   // null = イベントなし
    if (r == null) {
        r = new SurveyResultBean();
        r.setEventName("イベントがまだありません");
    }

    int[] ratingCounts = r.getRatingCounts();
    int ratingTotal = r.getRatingTotal();
    String backUrl = ctx + "/admin/EventDashboard.action" + (r.getEventId() > 0 ? "?eventId=" + r.getEventId() : "");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>アンケート結果 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/survey-result.css">
    <style>
        /* 「質問：回答」を1行ずつ */
        .qa-list {
            margin: 0;
            padding: 0;
            list-style: none;
        }
        .qa-list li + li {
            margin-top: 4px;
        }
        .qa-question {
            color: #71819a;
            font-size: 12px;
        }
        .muted {
            color: #a2afc1;
        }
    </style>
</head>
<body>
<%@ include file="/common/header.jsp" %>

<main class="survey-page">

    <!-- 1. 画面タイトル ／ 2. イベント名・回答数 ／ 3. 戻るボタン -->
    <div class="page-head">
        <div>
            <h1 class="page-title">アンケート結果</h1>
            <p class="page-sub"><%= h(r.getEventName()) %>　回答<%= r.getAnswerCount() %>件（参加者<%= r.getParticipantCount() %>名）</p>
        </div>
        <button type="button" name="backButton" class="back-button"
                onclick="location.href='<%= h(backUrl) %>'">
            ← イベント管理に戻る
        </button>
    </div>

    <!-- 4. 集計値（回答数・回答率・満足度の平均） -->
    <div class="kpi-row">
        <div class="kpi-card">
            <span class="kpi-title">回答数</span>
            <span class="kpi-value"><%= r.getAnswerCount() %><small>件</small></span>
        </div>
        <div class="kpi-card">
            <span class="kpi-title">回答率</span>
            <span class="kpi-value"><%= r.getAnswerRateText() %></span>
        </div>
        <div class="kpi-card">
            <span class="kpi-title">満足度（平均）</span>
            <span class="kpi-value"><%= r.getSatisfactionText() %><% if (ratingTotal > 0) { %><small>/ 5</small><% } %></span>
        </div>
    </div>

    <!-- 5. 満足度の内訳（横棒グラフ：★5 から ★1 まで） -->
    <section class="card">
        <h2 class="card-title">満足度（交流のしやすさ）の内訳</h2>
        <p class="card-sub">5段階評価（回答<%= ratingTotal %>件）</p>

<% for (int star = 5; star >= 1; star--) {
       int count = ratingCounts[star];
       int width = (ratingTotal > 0) ? count * 100 / ratingTotal : 0; %>
        <div class="bar-row">
            <span class="bar-label">★<%= star %></span>
            <div class="bar-track"><div class="bar" style="width: <%= width %>%;"></div></div>
            <span class="bar-value"><%= count %></span>
        </div>
<% } %>
    </section>

    <!-- 6. 回答一覧（新しい順） -->
    <section class="card">
        <h2 class="card-title">回答一覧（新しい順）</h2>

        <table class="answer-table">
            <thead>
                <tr>
                    <th>回答日時</th>
                    <th>満足度</th>
                    <th>アンケート回答</th>
                    <th>不満点・ご意見</th>
                </tr>
            </thead>
            <tbody>
<% if (r.getRows().isEmpty()) { %>
                <tr>
                    <td colspan="4" class="empty">まだアンケートの回答はありません</td>
                </tr>
<% } else { %>
<%     for (SurveyResultBean.Row row : r.getRows()) { %>
                <tr>
                    <td><%= h(row.getAnsweredAtText()) %></td>
                    <td><%= row.getRating() != null ? "★" + row.getRating() : "－" %></td>
                    <td>
                    <% if (row.getAnswers().isEmpty()) { %>
                        <span class="muted">－</span>
                    <% } else { %>
                        <ul class="qa-list">
                        <% for (String[] qa : row.getAnswers()) { %>
                            <li><span class="qa-question"><%= h(qa[0]) %></span><br><%= h(qa[1]) %></li>
                        <% } %>
                        </ul>
                    <% } %>
                    </td>
                    <td><%= (row.getComplaint() == null || row.getComplaint().isEmpty())
                            ? "<span class=\"muted\">－</span>" : h(row.getComplaint()) %></td>
                </tr>
<%     } %>
<% } %>
            </tbody>
        </table>
    </section>

</main>

<%@ include file="/common/footer.jsp" %>
