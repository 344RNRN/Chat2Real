<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.ArrayList, java.util.List" %>
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
    // ===== サーブレットから受け取るデータ（まだ無いときは 0・空にする） =====
    String eventName = (String) request.getAttribute("eventName");
    Integer participantCount = (Integer) request.getAttribute("participantCount");
    Integer answerCount = (Integer) request.getAttribute("answerCount");
    Double satisfactionAvg = (Double) request.getAttribute("satisfactionAvg");
    int[] ratingCounts = (int[]) request.getAttribute("ratingCounts");   // [1]～[5] に ★1～★5 の件数
    @SuppressWarnings("unchecked")
    List<String[]> answers = (List<String[]>) request.getAttribute("answers");   // {回答日時, 満足度, アンケート回答, 不満点}

    if (eventName == null) eventName = "";
    if (participantCount == null) participantCount = 0;
    if (answerCount == null) answerCount = 0;
    if (ratingCounts == null) ratingCounts = new int[6];
    if (answers == null) answers = new ArrayList<>();

    // 回答率・満足度の平均（計算できないときは「－」）
    String answerRateText = (participantCount > 0)
            ? String.format("%.2f", (double) answerCount / participantCount) : "－";
    String satisfactionText = (answerCount > 0 && satisfactionAvg != null)
            ? String.format("%.1f", satisfactionAvg) : "－";
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>アンケート結果 | Chat2Real</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/survey-result.css">
</head>
<body>
<%@ include file="/common/header.jsp" %>

<main class="survey-page">

    <!-- 1. 画面タイトル ／ 2. イベント名・回答数 ／ 3. 戻るボタン -->
    <div class="page-head">
        <div>
            <h1 class="page-title">アンケート結果</h1>
            <p class="page-sub"><%= h(eventName) %>　回答<%= answerCount %>件（参加者<%= participantCount %>名）</p>
        </div>
        <button type="button" name="backButton" class="back-button"
                onclick="location.href='<%= request.getContextPath() %>/EventDashboard.jsp'">
            ← イベント管理に戻る
        </button>
    </div>

    <!-- 4. 集計値（回答数・回答率・満足度の平均） -->
    <div class="kpi-row">
        <div class="kpi-card">
            <span class="kpi-title">回答数</span>
            <span class="kpi-value"><%= answerCount %><small>件</small></span>
        </div>
        <div class="kpi-card">
            <span class="kpi-title">回答率</span>
            <span class="kpi-value"><%= answerRateText %></span>
        </div>
        <div class="kpi-card">
            <span class="kpi-title">満足度（平均）</span>
            <span class="kpi-value"><%= satisfactionText %></span>
        </div>
    </div>

    <!-- 5. 満足度の内訳（横棒グラフ：★5 から ★1 まで） -->
    <section class="card">
        <h2 class="card-title">満足度（交流のしやすさ）の内訳</h2>
        <p class="card-sub">5段階評価（回答<%= answerCount %>件）</p>

<% for (int star = 5; star >= 1; star--) {
       int count = ratingCounts[star];
       int width = (answerCount > 0) ? count * 100 / answerCount : 0; %>
        <div class="bar-row">
            <span class="bar-label">★<%= star %></span>
            <div class="bar-track"><div class="bar" style="width: <%= width %>%;"></div></div>
            <span class="bar-value"><%= count %></span>
        </div>
<% } %>
    </section>

    <!-- 6. 自由記述の回答一覧（新しい順） -->
    <section class="card">
        <h2 class="card-title">自由記述の回答（新しい順）</h2>

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
<% if (answers.isEmpty()) { %>
                <tr>
                    <td colspan="4" class="empty">まだアンケートの回答はありません</td>
                </tr>
<% } else { %>
<%     for (String[] a : answers) { %>
                <tr>
                    <td><%= h(a[0]) %></td>
                    <td>★<%= h(a[1]) %></td>
                    <td><%= h(a[2]) %></td>
                    <td><%= h(a[3]) %></td>
                </tr>
<%     } %>
<% } %>
            </tbody>
        </table>
    </section>

</main>

<%@ include file="/common/footer.jsp" %>
