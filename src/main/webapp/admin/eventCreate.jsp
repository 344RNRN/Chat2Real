<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.ArrayList, java.util.List" %>
<%@ page import="Bean.InterestBean" %>
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
	// ===== サーブレットから受け取るデータ =====
	String accountName = (String) session.getAttribute("accountName");     // ログイン中の運営者名
	String errorMessage = (String) request.getAttribute("errorMessage");
	@SuppressWarnings("unchecked")
	List<InterestBean> interestList = (List<InterestBean>) request.getAttribute("interestList");
	// interestList：InterestDAO.findAllActive() の結果（t008_interests）

	if (accountName == null) accountName = "";
	if (interestList == null) interestList = new ArrayList<>();

	String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>イベント作成 | Chat2Real</title>
	<link rel="stylesheet" href="<%= ctx %>/css/event-create.css">
</head>
<body>

<%@ include file="/common/header.jsp" %>

<main class="create-page">

	<!-- 1. 画面タイトル ／ 運営者名・ログアウト -->
	<div class="create-head">
		<div>
			<span class="page-eyebrow">NEW EVENT</span>
			<h1 class="create-title">イベント作成</h1>
		</div>
		<div class="operator">
			<span>運営者：<strong><%= h(accountName) %></strong> さん</span>
			<form action="<%= ctx %>/admin/logout" method="post"
			      onsubmit="return confirm('ログアウトしますか？');">
				<button type="submit" class="logout-button">ログアウト</button>
			</form>
		</div>
	</div>

<% if (errorMessage != null) { %>
	<div class="error-message" role="alert"><%= h(errorMessage) %></div>
<% } %>
	<div class="error-message" id="clientError" role="alert" hidden></div>

	<form id="eventForm" action="<%= ctx %>/admin/EventCreate.action" method="post"
	      enctype="multipart/form-data" class="create-form" novalidate>

		<div class="create-grid">

			<!-- ===== 左：イベント情報 ===== -->
			<section class="card">
				<h2 class="card-title">イベント情報</h2>

				<!-- 2. イベント名 -->
				<div class="form-row">
					<label for="eventName" class="form-label required">イベント名</label>
					<input type="text" id="eventName" name="eventName" maxlength="150" required
					       placeholder="例：交流サポート秋祭り 2026"
					       value="<%= h(request.getParameter("eventName")) %>">
				</div>

				<!-- 3・4. 開催日・時間 -->
				<div class="form-row">
					<label for="eventDate" class="form-label required">開催日・時間</label>
					<div class="date-time">
						<input type="date" id="eventDate" name="eventDate" required
						       value="<%= h(request.getParameter("eventDate")) %>">
						<div class="time-range">
							<input type="time" id="startTime" name="startTime" required
							       aria-label="開始時間"
							       value="<%= h(request.getParameter("startTime")) %>">
							<span>～</span>
							<input type="time" id="endTime" name="endTime" required
							       aria-label="終了時間"
							       value="<%= h(request.getParameter("endTime")) %>">
						</div>
					</div>
				</div>

				<!-- 5. 会場 -->
				<div class="form-row">
					<label for="venue" class="form-label required">会場</label>
					<input type="text" id="venue" name="venue" maxlength="255" required
					       placeholder="例：みなとホール 3F"
					       value="<%= h(request.getParameter("venue")) %>">
				</div>

				<!-- 6. 定員 -->
				<div class="form-row">
					<label for="capacity" class="form-label">定員</label>
					<div class="capacity">
						<input type="number" id="capacity" name="capacity" min="1" max="9999"
						       placeholder="200"
						       value="<%= h(request.getParameter("capacity")) %>">
						<span>名</span>
					</div>
				</div>

				<!-- 7. イベント説明 -->
				<div class="form-row">
					<label for="eventDescription" class="form-label">イベント説明</label>
					<textarea id="eventDescription" name="eventDescription" rows="4" maxlength="1000"
					          placeholder="例：初対面の人と気軽に話せる交流会です。"><%= h(request.getParameter("eventDescription")) %></textarea>
				</div>

				<!-- 8・9. 資料 -->
				<div class="form-row">
					<span class="form-label">資料</span>
					<div>
						<div class="file-picker">
							<input type="text" id="fileNames" readonly
							       placeholder="画像・PDF（1ファイル10MB以内）" aria-label="選択した資料">
							<button type="button" class="sub-button" id="browseButton">参照</button>
							<input type="file" id="materials" name="materials" multiple
							       accept="image/*,application/pdf" hidden>
						</div>
					</div>
				</div>

				<!-- 10・11. 興味タグ -->
				<div class="form-row">
					<span class="form-label">興味タグ</span>
					<div>
						<div class="tag-container" id="tagContainer">
						<% for (InterestBean tag : interestList) { %>
							<label class="tag">
								<input type="checkbox" name="interestIds" value="<%= tag.getInterestId() %>">
								<span><%= h(tag.getInterestName()) %></span>
							</label>
						<% } %>
							<button type="button" class="tag-add-button" id="tagAddButton">＋ 追加</button>
						</div>

						<!-- 「＋追加」を押すと表示される入力欄 -->
						<div class="tag-input" id="tagInputArea" hidden>
							<input type="text" id="newTagInput" maxlength="50"
							       placeholder="新しいタグ名" aria-label="新しいタグ名">
							<button type="button" class="sub-button" id="tagConfirmButton">追加</button>
						</div>
					</div>
				</div>
			</section>

			<!-- ===== 右：12. 参加者への質問 ===== -->
			<section class="card">
				<h2 class="card-title">参加者への質問（任意）</h2>
				<p class="card-sub">
					ここで設定した質問は、イベント終了後の「終了レポート（アンケート）」画面に表示されます。
				</p>

				<div id="questionList"></div>

				<!-- 18. 質問を追加 -->
				<button type="button" class="add-question-button" id="addQuestionButton">
					＋ 質問を追加（<span id="questionCounter">0</span> / 5）
				</button>

				<input type="hidden" name="questionCount" id="questionCount" value="0">

				<p class="card-note">回答はアンケート結果画面で集計・確認できます。</p>
			</section>

		</div>

		<!-- 19・20. キャンセル・作成 -->
		<div class="form-actions">
			<button type="button" class="cancel-button" id="cancelButton">キャンセル</button>
			<button type="submit" name="createButton" class="create-button">イベントを作成</button>
		</div>

	</form>

</main>

<!-- 質問ブロックのひな形（JS でコピーして使う） -->
<template id="questionTemplate">
	<div class="question-block">
		<div class="question-head">
			<span class="question-number">質問1</span>
			<!-- 17. 削除 -->
			<button type="button" class="delete-button">削除</button>
		</div>

		<!-- 13. 質問文 -->
		<input type="text" data-name="text" maxlength="500" required
		       placeholder="例：今日いちばん話したいテーマは？" aria-label="質問文">

		<!-- 14. 回答形式 -->
		<div class="question-type">
			<span class="mini-label">回答形式</span>
			<label><input type="radio" data-name="type" value="select" checked> 選択式</label>
			<label><input type="radio" data-name="type" value="text"> 自由記述</label>
		</div>

		<!-- 15. 選択肢（選択式のときだけ表示） -->
		<div class="choices-row">
			<span class="mini-label">選択肢</span>
			<input type="text" data-name="options" maxlength="500"
			       placeholder="「／」で区切る　例：仕事・キャリア／趣味／地元の話" aria-label="選択肢">
		</div>

		<!-- 16. 回答必須 -->
		<label class="required-check">
			<input type="checkbox" data-name="required" value="true"> 回答必須
		</label>
	</div>
</template>

<%@ include file="/common/footer.jsp" %>

<script>
(function () {
	const MAX_QUESTIONS = 5;
	const MAX_FILE_SIZE = 10 * 1024 * 1024;   // 10MB

	const form = document.getElementById("eventForm");
	const clientError = document.getElementById("clientError");

	function showError(message) {
		clientError.textContent = message;
		clientError.hidden = false;
		clientError.scrollIntoView({ behavior: "smooth", block: "center" });
	}

	/* ---------- 8・9. 資料（参照ボタン） ---------- */
	const fileInput = document.getElementById("materials");
	const fileNames = document.getElementById("fileNames");

	document.getElementById("browseButton").addEventListener("click", function () {
		fileInput.click();
	});

	fileInput.addEventListener("change", function () {
		const files = Array.from(fileInput.files);
		for (const f of files) {
			const okType = f.type.startsWith("image/") || f.type === "application/pdf";
			if (!okType) {
				showError("資料は画像またはPDFを選択してください：" + f.name);
				fileInput.value = ""; fileNames.value = ""; return;
			}
			if (f.size > MAX_FILE_SIZE) {
				showError("10MBを超えるファイルは追加できません：" + f.name);
				fileInput.value = ""; fileNames.value = ""; return;
			}
		}
		clientError.hidden = true;
		fileNames.value = files.map(f => f.name).join("、");
	});

	/* ---------- 10・11. 興味タグの追加 ---------- */
	const tagContainer = document.getElementById("tagContainer");
	const tagAddButton = document.getElementById("tagAddButton");
	const tagInputArea = document.getElementById("tagInputArea");
	const newTagInput = document.getElementById("newTagInput");

	tagAddButton.addEventListener("click", function () {
		tagInputArea.hidden = false;
		newTagInput.focus();
	});

	function addTag() {
		const name = newTagInput.value.trim();
		if (!name) return;

		const exists = Array.from(tagContainer.querySelectorAll(".tag span"))
			.some(span => span.textContent === name);
		if (exists) {
			showError("「" + name + "」はすでにあります");
			return;
		}

		const label = document.createElement("label");
		label.className = "tag";
		const cb = document.createElement("input");
		cb.type = "checkbox";
		cb.name = "newInterests";      // 新しいタグはサーブレットで t008_interests に登録
		cb.value = name;
		cb.checked = true;
		const span = document.createElement("span");
		span.textContent = name;
		label.append(cb, span);
		tagContainer.insertBefore(label, tagAddButton);

		newTagInput.value = "";
		tagInputArea.hidden = true;
		clientError.hidden = true;
	}

	document.getElementById("tagConfirmButton").addEventListener("click", addTag);
	newTagInput.addEventListener("keydown", function (e) {
		if (e.key === "Enter") { e.preventDefault(); addTag(); }   // Enter で送信しない
	});

	/* ---------- 12~18. 参加者への質問 ---------- */
	const questionList = document.getElementById("questionList");
	const template = document.getElementById("questionTemplate");
	const addQuestionButton = document.getElementById("addQuestionButton");

	// 番号を振り直し、name を q1_text / q1_type / q1_options / q1_required … にする
	function renumber() {
		const blocks = questionList.querySelectorAll(".question-block");
		blocks.forEach(function (block, i) {
			const n = i + 1;
			block.querySelector(".question-number").textContent = "質問" + n;
			block.querySelectorAll("[data-name]").forEach(function (el) {
				el.name = "q" + n + "_" + el.dataset.name;
			});
		});
		document.getElementById("questionCount").value = blocks.length;
		document.getElementById("questionCounter").textContent = blocks.length;
		addQuestionButton.disabled = blocks.length >= MAX_QUESTIONS;
	}

	function updateChoices(block) {
		const isSelect = block.querySelector('[data-name="type"]:checked').value === "select";
		const row = block.querySelector(".choices-row");
		row.hidden = !isSelect;
		row.querySelector("input").required = isSelect;
	}

	function addQuestion() {
		if (questionList.children.length >= MAX_QUESTIONS) return;

		const block = template.content.firstElementChild.cloneNode(true);

		block.querySelectorAll('[data-name="type"]').forEach(function (radio) {
			radio.addEventListener("change", function () { updateChoices(block); });
		});

		block.querySelector(".delete-button").addEventListener("click", function () {
			block.remove();
			renumber();
		});

		questionList.appendChild(block);
		renumber();
		updateChoices(block);
	}

	addQuestionButton.addEventListener("click", addQuestion);
	renumber();   // 質問は任意なので最初は0問（「＋ 質問を追加」で増やす）

	/* 入力欄の名前（エラーメッセージ用）：ラベル → aria-label の順に探す */
	function fieldName(el) {
		if (!el) return "";
		let name = "";
		if (el.labels && el.labels.length > 0) {
			name = el.labels[0].textContent.replace("※", "").trim();
		} else if (el.getAttribute("aria-label")) {
			name = el.getAttribute("aria-label");
		}
		const block = el.closest(".question-block");
		if (block) {
			name = block.querySelector(".question-number").textContent + "の" + name;
		}
		return name;
	}

	/* ---------- 送信前チェック ---------- */
	form.addEventListener("submit", function (e) {
		clientError.hidden = true;

		if (!form.checkValidity()) {
			e.preventDefault();
			const first = form.querySelector(":invalid");
			showError("未入力の項目があります：" + fieldName(first));
			if (first) first.focus();
			return;
		}

		const start = document.getElementById("startTime").value;
		const end = document.getElementById("endTime").value;
		if (start && end && end <= start) {
			e.preventDefault();
			showError("終了時間は開始時間より後にしてください");
			return;
		}

		if (!confirm("イベントを作成しますか？")) {
			e.preventDefault();
		}
	});

	/* ---------- 19. キャンセル ---------- */
	document.getElementById("cancelButton").addEventListener("click", function () {
		if (confirm("入力内容を破棄して、イベント管理画面に戻りますか？")) {
			location.href = "<%= ctx %>/admin/EventDashboard.jsp";
		}
	});
})();
</script>
