<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="Bean.TopScreenBean" %>
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

	// TopScreenAction を通さずに開いたとき（ChangeStatus から戻ってきたときなど）は Action から開き直す
	TopScreenBean top = (TopScreenBean) request.getAttribute("top");
	if (top == null) {
		response.sendRedirect(ctx + "/participant/Top.action");
		return;
	}

	// ==== サーブレット（TopScreenAction）から受け取るデータ ====
	String eventName = (String) request.getAttribute("eventName");
	String eventDate = (String) request.getAttribute("eventDate");
	String venue = (String) request.getAttribute("venue");
	Integer participantCount = (Integer) request.getAttribute("participantCount");
	Integer wantCount = (Integer) request.getAttribute("wantCount");
	String noticeTitle = (String) request.getAttribute("noticeTitle");
	String noticeText = (String) request.getAttribute("noticeText");
	String exchangeStatus = (String) request.getAttribute("exchangeStatus");
	String displayName = (String) request.getAttribute("displayName");

	if (eventName == null) eventName = "";
	if (eventDate == null) eventDate = "－";
	if (venue == null) venue = "－";
	if (participantCount == null) participantCount = 0;
	if (wantCount == null) wantCount = 0;
	if (displayName == null) displayName = "nickname";

	boolean isWant = "交流したい".equals(exchangeStatus);
	boolean isNotNow = "今は交流したくない".equals(exchangeStatus);
%>
<!DOCTYPE html>
<html lang="ja">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>イベントルーム | Chat2Real</title>
	<link rel="stylesheet" href="<%= ctx %>/css/top.css?v=2">
</head>
<body class="top-screen">

<%-- 参加者用ヘッダー（参加者一覧と同じ）※ <body> の中に入れる --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="top-page">

	<!-- ページ見出し -->
	<section class="page-heading">
		<span class="page-eyebrow">EVENT ROOM</span>
		<h1>イベントルーム</h1>
		<p class="page-description">ようこそ、<strong><%= h(top.getNickname()) %></strong> さん。今日の交流を楽しみましょう。</p>
	</section>

	<!-- 1. お知らせバナー（お知らせがある時だけ表示） -->
	<% if (noticeText != null) { %>
	<a class="notice-banner" href="<%= ctx %>/participant/Lounge.action">
		<span class="notice-icon">!</span>
		<span class="notice-body">
			<span class="notice-label">運営者からのお知らせ<% if (noticeTitle != null) { %>：<%= h(noticeTitle) %><% } %></span>
			<span class="notice-text"><%= h(noticeText) %></span>
		</span>
		<span class="link-arrow">→</span>
	</a>
	<% } %>

	<!-- 2~5. イベント情報 -->
	<section class="event-card">
		<h2 class="event-name"><%= h(eventName) %></h2>
		<p class="event-info"><span class="info-icon" aria-hidden="true">🗓</span><%= h(eventDate) %></p>
		<p class="event-info"><span class="info-icon" aria-hidden="true">📍</span><%= h(venue) %></p>

		<div class="event-stats">
			<div class="stat">
				<span class="stat-number"><%= participantCount %></span>
				<span class="stat-label">参加者</span>
			</div>
			<div class="stat stat-want">
				<span class="stat-number"><%= wantCount %></span>
				<span class="stat-label">交流したい</span>
			</div>
		</div>
	</section>

	<!-- 6~9. 今の気分（交流したい/今は交流したくない） -->
	<section class="mood">
		<p class="section-title">今の気分を選んでください</p>
		<form action="<%= ctx %>/participant/ChangeStatus.action" method="post" class="mood-buttons">
			<button type="submit" name="wantButton" value="交流したい"
				class="mood-button want<%= isWant ? " selected" : "" %>">
				<span class="mood-emoji" aria-hidden="true">💬</span>交流したい
			</button>
			<button type="submit" name="notNowButton" value="今は交流したくない"
				class="mood-button not-now<%= isNotNow ? " selected" : "" %>">
				<span class="mood-emoji" aria-hidden="true">☕</span>今は交流したくない
			</button>
		</form>
		<p class="mood-note">
			「交流したい」を選ぶと参加者一覧とチャットが使えます。「今は交流したくない」を選ぶと運営者からの資料が表示されます。いつでも切り替えられます。
		</p>
		<p class="current-status">現在：
	<% if (isWant) { %>
			<span class="status want">交流したい</span>
	<% } else if (isNotNow) { %>
			<span class="status not-now">今は交流したくない</span>
	<% } else { %>
			<span class="status none">未選択</span>
	<% } %>
		</p>
	</section>

	<!-- 10. 表示名切り替え（ニックネーム/本名） -->
	<section class="name-switch">
		<p class="section-title">他の参加者に表示する名前</p>
		<form action="<%= ctx %>/participant/ChangeDisplayName.action" method="post" class="name-buttons">
			<button type="submit" name="displayName" value="nickname"
				class="name-button<%= "nickname".equals(displayName) ? " selected" : "" %>">
				ニックネーム<span class="name-preview"><%= h(top.getNickname()) %></span>
			</button>
			<button type="submit" name="displayName" value="realName"
				class="name-button<%= "realName".equals(displayName) ? " selected" : "" %>"
				<%= top.hasRealName() ? "" : "disabled title=\"本名が登録されていません\"" %>>
				本名<span class="name-preview"><%= top.hasRealName() ? h(top.getRealName()) : "未登録" %></span>
			</button>
		</form>
	</section>

	<!-- 11. 下のリンク（イベント資料・退出） -->
	<nav class="bottom-links">
		<a href="<%= ctx %>/participant/MaterialsView.action" class="materials-link">
			イベント資料 <span class="link-arrow">→</span>
		</a>
		<a href="<%= ctx %>/participant/Leave.action" class="leave-link"
		   onclick="return confirm('イベントから退出しますか？');">退出</a>
	</nav>

</main>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
