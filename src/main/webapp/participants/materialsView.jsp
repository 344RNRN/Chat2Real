<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="Bean.LoungeBean" %>
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
        return s == null || s.isEmpty();
    }
%>
<%
    String ctx = request.getContextPath();

    // ===== サーブレット（MaterialsViewAction）から受け取るデータ =====
    LoungeBean m = (LoungeBean) request.getAttribute("materials");
    if (m == null) {
        // Action を通さずに開いたときは Action から開き直す
        response.sendRedirect(ctx + "/participant/MaterialsView.action");
        return;
    }

    // お知らせ・資料の件数
    int noticeCount = 0;
    int fileCount = 0;
    for (LoungeBean.Item it : m.getItems()) {
        if (it.isNotice()) {
            noticeCount++;
            if (it.hasFile()) fileCount++;
        } else {
            fileCount++;
        }
    }
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>イベント資料 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/materials-view.css">
</head>
<body class="materials-view-screen">

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="materials-view-page">

    <!-- 戻る -->
    <a href="<%= ctx %><%= m.isWant() ? "/participant/ParticipantList.action" : "/participant/Top.action" %>"
       class="back-link">‹ <%= m.isWant() ? "参加者一覧に戻る" : "イベントルームに戻る" %></a>

    <!-- 見出し -->
    <section class="page-heading">
        <span class="page-eyebrow">MATERIALS</span>
        <h1>イベント資料</h1>
        <p class="page-description"><%= h(m.getEventName()) %></p>

        <div class="summary">
            <span class="summary-chip notice">告知 <strong><%= noticeCount %></strong></span>
            <span class="summary-chip file">ファイル <strong><%= fileCount %></strong></span>
        </div>
    </section>

    <!-- お知らせ・資料（新しい順） -->
    <section class="panel">
        <h2 class="panel-title"><span aria-hidden="true">📄</span> お知らせ・資料</h2>

    <% if (m.getItems().isEmpty()) { %>
        <div class="panel-empty">
            <span class="empty-icon" aria-hidden="true">📭</span>
            <p>まだお知らせ・資料はありません。<br>運営者から配信されると、ここに表示されます。</p>
        </div>
    <% } else { %>
        <ul class="doc-list">
        <% for (LoungeBean.Item it : m.getItems()) { %>
            <li class="doc-item">
                <span class="doc-icon kind-<%= it.isNotice() ? "notice" : "file" %>"><%= h(it.getKindLabel()) %></span>

                <div class="doc-body">
                <% if (it.isNotice()) { %>
                    <p class="doc-title"><%= h(it.getTitle()) %></p>
                    <p class="doc-text"><%= h(it.getText()) %></p>

                <% if (it.hasFile()) { %>
                    <a class="doc-attachment" href="<%= ctx %><%= h(it.getFilePath()) %>" target="_blank" rel="noopener">
                        <span class="attachment-kind"><%= h(it.getFileLabel()) %></span>
                        <span class="attachment-name"><%= h(it.getFileName()) %></span>
                        <span class="attachment-open">開く ↗</span>
                    </a>
                <% } %>

                    <span class="doc-meta">運営者からのお知らせ<%= isBlank(it.getTimeText()) ? "" : "・" + h(it.getTimeText()) %></span>
                <% } else { %>
                    <a class="doc-attachment file-only" href="<%= ctx %><%= h(it.getFilePath()) %>" target="_blank" rel="noopener">
                        <span class="attachment-name"><%= h(it.getTitle()) %></span>
                        <span class="attachment-open">開く ↗</span>
                    </a>
                    <span class="doc-meta">運営者<%= isBlank(it.getTimeText()) ? "" : "・" + h(it.getTimeText()) %></span>
                <% } %>
                </div>
            </li>
        <% } %>
        </ul>
    <% } %>
    </section>

    <p class="page-note">運営者からのお知らせ・資料が新しい順に表示されます。ファイルは別のタブで開きます。</p>

    <!-- 下のリンク -->
    <nav class="bottom-links">
    <% if (m.isWant()) { %>
        <a href="<%= ctx %>/participant/ParticipantList.action" class="main-link">
            <span aria-hidden="true">🧑‍🤝‍🧑</span> 参加者一覧へ
        </a>
    <% } %>
        <a href="<%= ctx %>/participant/Top.action" class="sub-link">イベントルームへ</a>
    </nav>

</main>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
