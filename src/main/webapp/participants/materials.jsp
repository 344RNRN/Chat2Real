<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="Bean.MaterialsBean" %>
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

    // サーバー内のファイル（/uploads/...）のときだけリンクにする
    private static boolean isLocalPath(String path) {
        return path != null && path.startsWith("/") && !path.startsWith("//");
    }
%>
<%
    // ===== サーブレット（MaterialsAction）から受け取るデータ =====
    MaterialsBean m = (MaterialsBean) request.getAttribute("materials");   // null：イベントが見つからない

    // 「今は交流したくない」を選んだ人に表示する画面なので、何もなければ「今はしない」を選択中にする
    boolean isWant = m != null && "交流したい".equals(m.getExchangeStatus());

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>イベント資料 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/materials.css">
</head>
<body>

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="materials-page">

    <a href="<%= ctx %>/admin/topScreen.jsp" class="back-link">‹ 戻る</a>

    <!-- ページ見出し -->
    <section class="page-heading">
        <span class="page-eyebrow">MATERIALS</span>
        <h1>イベント資料</h1>
<% if (m != null) { %>
        <p class="page-description"><%= h(m.getEventName()) %></p>
<% } %>
    </section>

<% if (m == null) { %>

    <section class="panel">
        <p class="empty-message">イベントの情報がありません。</p>
    </section>

<% } else { %>

    <!-- 1, 2. 交流したい／今はしない -->
    <form action="<%= ctx %>/participant/ChangeStatus.action" method="post" class="mood-buttons">
        <button type="submit" name="wantButton" value="交流したい"
                class="mood-button want<%= isWant ? " selected" : "" %>">交流したい</button>
        <button type="submit" name="notNowButton" value="今は交流したくない"
                class="mood-button not-now<%= isWant ? "" : " selected" %>">今はしない</button>
    </form>

    <!-- 3～7. お知らせ・資料（新しい順） -->
    <section class="panel">
        <h2 class="section-title">お知らせ・資料</h2>

<% if (m.getNotices().isEmpty() && m.getMaterials().isEmpty()) { %>
        <p class="empty-message">まだお知らせ・資料はありません。</p>
<% } else { %>
        <ul class="item-list">

<% for (MaterialsBean.Material f : m.getMaterials()) { %>
            <li class="item">
                <!-- 4. 種別アイコン -->
                <span class="kind-icon file"><%= h(f.getKindLabel()) %></span>
                <div class="item-body">
                    <!-- 5. タイトル -->
                    <p class="item-title"><%= h(f.getFileName()) %></p>
                    <!-- 6. 投稿者・時刻 -->
                    <span class="item-meta">運営者<%= f.getTimeText().isEmpty() ? "" : "・" + h(f.getTimeText()) %></span>
                    <!-- 7. 添付ファイル（別タブで開く） -->
<% if (isLocalPath(f.getFilePath())) { %>
                    <a href="<%= ctx %><%= h(f.getFilePath()) %>" target="_blank" rel="noopener"
                       class="file-link"><%= h(f.getFileName()) %> を開く</a>
<% } %>
                </div>
            </li>
<% } %>

<% for (MaterialsBean.Notice n : m.getNotices()) { %>
            <li class="item">
                <span class="kind-icon notice">告知</span>
                <div class="item-body">
                    <p class="item-title"><%= h(n.getTitle()) %></p>
                    <p class="item-text"><%= h(n.getText()) %></p>
                    <span class="item-meta">運営者</span>
                </div>
            </li>
<% } %>

        </ul>
<% } %>
    </section>

    <!-- 8. 説明文 -->
    <p class="materials-note">運営者からのお知らせ・資料が新しい順に表示されます。</p>

<% } %>

</main>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
