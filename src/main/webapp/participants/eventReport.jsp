<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.ArrayList, java.util.List" %>
<%@ page import="Bean.EventReportBean" %>
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
	// ===== サーブレットから受け取るデータ（まだ無いときは 0・空にする） =====
	String eventName = (String) request.getAttribute("eventName");
	Integer meetupCount = (Integer) request.getAttribute("meetupCount");      // 直接交流できた人数
	@SuppressWarnings("unchecked")
	List<EventReportBean.Question> questions = (List<EventReportBean.Question>) request.getAttribute("questions");
	// questions：EventReportDAO.findReport() の結果（イベント作成画面で設定した質問）
	Boolean answered = (Boolean) request.getAttribute("answered");            // 回答済みなら true
	String errorMessage = (String) request.getAttribute("errorMessage");

	if (eventName == null) eventName = "";
	if (meetupCount == null) meetupCount = 0;
	if (questions == null) questions = new ArrayList<>();
	if (answered == null) answered = false;

	String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>終了レポート | Chat2Real</title>
	<link rel="stylesheet" href="<%= ctx %>/css/event-report.css">
</head>
<body>

<%@ include file="/common/header-participants.jsp" %>

<main class="report-page">

	<!-- 画面タイトル・イベント名 -->
	<section class="page-heading">
		<span class="page-eyebrow">EVENT REPORT</span>
		<h1>アンケート</h1>
		<p class="page-description"><%= h(eventName) %></p>
	</section>

	<!-- 1・2. 直接交流できた人数 -->
	<section class="result-card">
		<p class="result-number"><%= meetupCount %><span class="result-unit">人</span></p>
		<p class="result-text">
			<% if (meetupCount > 0) { %>
				と直接交流できました
			<% } else { %>
				今回は直接交流はありませんでした。次回もお待ちしています！
			<% } %>
		</p>
	</section>

<% if (answered) { %>

	<!-- 回答済みのとき -->
	<section class="survey-card done-card">
		<p class="done-icon">✓</p>
		<p class="done-title">アンケートを送信しました</p>
		<p class="done-text">ご協力ありがとうございました。</p>
	</section>

<% } else { %>

	<!-- 3~11. アンケート -->
	<section class="survey-card">

	<% if (errorMessage != null) { %>
		<div class="error-message" role="alert"><%= h(errorMessage) %></div>
	<% } %>

		<form action="<%= ctx %>/participant/SurveyAnswer.action" method="post" class="survey-form">

			<!-- 運営者が設定したアンケート質問（t004_survey_questions） -->
		<% for (EventReportBean.Question q : questions) {
			   long qId = q.getQuestionId();
			   boolean required = q.isRequired(); %>
		<% if (q.isSelect()) { %>
			<!-- 選択式：イベント作成画面の「選択肢」を「／」で区切ってボタンにする -->
			<fieldset class="form-group choice-group">
				<legend class="<%= required ? "required" : "" %>"><%= h(q.getQuestionText()) %></legend>
				<div class="choice-list">
				<% boolean first = true;
				   for (String opt : q.getOptionList()) { %>
					<label class="choice">
						<input type="radio" name="answer_<%= qId %>" value="<%= h(opt) %>"
							<%= (required && first) ? "required" : "" %>>
						<span><%= h(opt) %></span>
					</label>
				<%     first = false;
				   } %>
				</div>
			</fieldset>
		<% } else { %>
			<div class="form-group">
				<label for="answer_<%= qId %>" class="<%= required ? "required" : "" %>">
					<%= h(q.getQuestionText()) %>
				</label>
				<input type="text" id="answer_<%= qId %>" name="answer_<%= qId %>"
					maxlength="200" <%= required ? "required" : "" %>
					placeholder="回答を入力してください">
			</div>
		<% } %>
		<% } %>

			<!-- 不満点・ご意見 -->
			<div class="form-group">
				<label for="complaint">不満点がございましたらお書きください</label>
				<textarea id="complaint" name="complaint" rows="3" maxlength="500"
					placeholder="例：会場が少し騒がしかった など"></textarea>
			</div>

			<!-- 10. 満足度（★1～★5） -->
			<fieldset class="form-group rating-group">
				<legend class="required">今日の交流のしやすさは？（満足度アンケート）</legend>
				<div class="star-rating">
					<% for (int star = 5; star >= 1; star--) { %>
					<input type="radio" id="star<%= star %>" name="satisfaction"
						value="<%= star %>" <%= star == 5 ? "required" : "" %>>
					<label for="star<%= star %>" title="★<%= star %>"
						aria-label="<%= star %>点">★</label>
					<% } %>
				</div>
				<p class="rating-hint" id="ratingHint">星をタップして評価してください</p>
			</fieldset>

			<!-- 11. 送信ボタン -->
			<button type="submit" name="sendButton" class="submit-button">
				アンケートを送信
			</button>

		</form>
	</section>

<% } %>

	<!-- 12. 終了のお知らせ -->
	<section class="closing-note">
		<p>
			ご参加ありがとうございました。今回のチャット・参加者情報は本日中に完全削除されます（次回イベントには引き継がれません）。
		</p>
	</section>

</main>

<%@ include file="/common/footer.jsp" %>

<script>
	// 選んだ星の数を下に表示する
	const hint = document.getElementById("ratingHint");
	document.querySelectorAll('input[name="satisfaction"]').forEach(function (radio) {
		radio.addEventListener("change", function () {
			if (hint) hint.textContent = "あなたの評価：★" + this.value;
		});
	});
</script>
