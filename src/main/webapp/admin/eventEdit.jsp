<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.ArrayList, java.util.List" %>
<%@ page import="Bean.InterestBean, Bean.EventEditBean" %>
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
	EventEditBean ev = (EventEditBean) request.getAttribute("event");
	String errorMessage = (String) request.getAttribute("errorMessage");
	@SuppressWarnings("unchecked")
	List<InterestBean> interestList = (List<InterestBean>) request.getAttribute("interestList");
	String ctx = request.getContextPath();

	// Action を通さずに JSP を開いたときはダッシュボードへ
	if (ev == null) {
		response.sendRedirect(ctx + "/admin/EventDashboard.action");
		return;
	}
	if (interestList == null) interestList = new ArrayList<>();

	boolean isPost = "POST".equalsIgnoreCase(request.getMethod());
	String capacityDb = ev.getCapacity() == null ? "" : String.valueOf(ev.getCapacity());

	// 保存でエラーになって戻ってきたときは入力した値、最初に開いたときは DB の値
	String vName  = isPost ? request.getParameter("eventName")        : ev.getEventName();
	String vDate  = isPost ? request.getParameter("eventDate")        : ev.getDateValue();
	String vStart = isPost ? request.getParameter("startTime")        : ev.getStartTimeValue();
	String vEnd   = isPost ? request.getParameter("endTime")          : ev.getEndTimeValue();
	String vVenue = isPost ? request.getParameter("venue")            : ev.getVenue();
	String vCap   = isPost ? request.getParameter("capacity")         : capacityDb;
	String vDesc  = isPost ? request.getParameter("eventDescription") : ev.getEventDescription();
%>
<!DOCTYPE html>
<html lang="ja">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>イベント編集 | Chat2Real</title>
	<link rel="stylesheet" href="<%= ctx %>/css/event-create.css">
	<style>
		/* 登録済み資料の一覧 */
		.material-list {
			list-style: none;
			margin: 0 0 10px;
			padding: 0;
		}
		.material-list li {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 10px;
			padding: 9px 12px;
			margin-bottom: 6px;
			background: #f8faff;
			border: 1px solid var(--border);
			border-radius: 10px;
			font-size: 14px;
		}
		.material-list a {
			color: #2563eb;
			overflow-wrap: anywhere;
		}
		.material-delete {
			display: inline-flex;
			align-items: center;
			gap: 5px;
			flex-shrink: 0;
			color: #c62828;
			font-size: 13px;
			font-weight: 700;
			cursor: pointer;
		}
		.material-delete input {
			accent-color: #c62828;
		}
		.material-list li:has(input:checked) a {
			text-decoration: line-through;
			opacity: 0.55;
		}
		/* 回答があって質問を変更できないとき */
		.locked-note {
			margin: 0 0 14px;
			padding: 12px 14px;
			color: #8a5a00;
			background: #fff8e6;
			border: 1px solid #f6dfa4;
			border-radius: 10px;
			font-size: 13px;
			line-height: 1.7;
		}
		.locked-list {
			margin: 0;
			padding-left: 20px;
			font-size: 14px;
			line-height: 1.9;
		}
		.locked-list small {
			color: var(--muted);
		}
	</style>
</head>
<body>

<%@ include file="/common/header.jsp" %>

<main class="create-page">

	<!-- タイトル -->
	<div class="create-head">
		<div>
			<span class="page-eyebrow">EDIT EVENT</span>
			<h1 class="create-title">イベント編集</h1>
		</div>
	</div>

<% if (errorMessage != null) { %>
	<div class="error-message" role="alert"><%= h(errorMessage) %></div>
<% } %>
	<div class="error-message" id="clientError" role="alert" hidden></div>

	<form id="eventForm" action="<%= ctx %>/admin/EventEdit.action" method="post"
	      enctype="multipart/form-data" class="create-form" novalidate>

		<input type="hidden" name="eventId" value="<%= ev.getEventId() %>">

		<div class="create-grid">

			<!-- ===== 左：イベント情報 ===== -->
			<section class="card">
				<h2 class="card-title">イベント情報</h2>

				<div class="form-row">
					<label for="eventName" class="form-label required">イベント名</label>
					<input type="text" id="eventName" name="eventName" maxlength="150" required
					       placeholder="例：交流サポート秋祭り 2026" value="<%= h(vName) %>">
				</div>

				<div class="form-row">
					<label for="eventDate" class="form-label required">開催日・時間</label>
					<div class="date-time">
						<input type="date" id="eventDate" name="eventDate" required value="<%= h(vDate) %>">
						<div class="time-range">
							<input type="time" id="startTime" name="startTime" required
							       aria-label="開始時間" value="<%= h(vStart) %>">
							<span>～</span>
							<input type="time" id="endTime" name="endTime" required
							       aria-label="終了時間" value="<%= h(vEnd) %>">
						</div>
					</div>
				</div>

				<div class="form-row">
					<label for="venue" class="form-label required">会場</label>
					<input type="text" id="venue" name="venue" maxlength="255" required
					       placeholder="例：みなとホール 3F" value="<%= h(vVenue) %>">
				</div>

				<div class="form-row">
					<label for="capacity" class="form-label">定員</label>
					<div class="capacity">
						<input type="number" id="capacity" name="capacity" min="1" max="9999"
						       placeholder="200" value="<%= h(vCap) %>">
						<span>名</span>
					</div>
				</div>

				<div class="form-row">
					<label for="eventDescription" class="form-label">イベント説明</label>
					<textarea id="eventDescription" name="eventDescription" rows="4" maxlength="1000"
					          placeholder="例：初対面の人と気軽に話せる交流会です。"><%= h(vDesc) %></textarea>
				</div>

				<!-- 資料：登録済み（削除にチェック）＋ 新しく追加 -->
				<div class="form-row">
					<span class="form-label">資料</span>
					<div>
					<% if (!ev.getMaterials().isEmpty()) { %>
						<ul class="material-list">
						<% for (EventEditBean.Material m : ev.getMaterials()) { %>
							<li>
								<a href="<%= ctx %><%= h(m.getFilePath()) %>" target="_blank" rel="noopener"><%= h(m.getFileName()) %></a>
								<label class="material-delete">
									<input type="checkbox" name="deleteMaterialIds" value="<%= m.getMaterialId() %>"
										<%= ev.getDeleteMaterialIds().contains(m.getMaterialId()) ? "checked" : "" %>>
									削除
								</label>
							</li>
						<% } %>
						</ul>
					<% } %>
						<div class="file-picker">
							<input type="text" id="fileNames" readonly
							       placeholder="追加する資料：画像・PDF（1ファイル10MB以内）" aria-label="追加する資料">
							<button type="button" class="sub-button" id="browseButton">参照</button>
							<input type="file" id="materials" name="materials" multiple
							       accept="image/*,application/pdf" hidden>
						</div>
					</div>
				</div>

				<!-- 興味タグ -->
				<div class="form-row">
					<span class="form-label">興味タグ</span>
					<div>
						<div class="tag-container" id="tagContainer">
						<% for (InterestBean tag : interestList) { %>
							<label class="tag">
								<input type="checkbox" name="interestIds" value="<%= tag.getInterestId() %>"
									<%= ev.getInterestIds().contains(tag.getInterestId()) ? "checked" : "" %>>
								<span><%= h(tag.getInterestName()) %></span>
							</label>
						<% } %>
						<% for (String name : ev.getNewInterests()) { %>
							<label class="tag">
								<input type="checkbox" name="newInterests" value="<%= h(name) %>" checked>
								<span><%= h(name) %></span>
							</label>
						<% } %>
							<button type="button" class="tag-add-button" id="tagAddButton">＋ 追加</button>
						</div>

						<div class="tag-input" id="tagInputArea" hidden>
							<input type="text" id="newTagInput" maxlength="50"
							       placeholder="新しいタグ名" aria-label="新しいタグ名">
							<button type="button" class="sub-button" id="tagConfirmButton">追加</button>
						</div>
					</div>
				</div>
			</section>

			<!-- ===== 右：参加者への質問 ===== -->
			<section class="card">
				<h2 class="card-title">参加者への質問（任意）</h2>
				<p class="card-sub">
					ここで設定した質問は、イベント終了後の「終了レポート（アンケート）」画面に表示されます。
				</p>

			<% if (ev.isQuestionsLocked()) { %>
				<!-- すでに回答があるので変更できない -->
				<p class="locked-note">
					すでにアンケートの回答があるため、質問は変更できません（回答の集計が合わなくなるため）。
				</p>
				<% if (ev.getQuestions().isEmpty()) { %>
					<p class="card-note">質問はありません。</p>
				<% } else { %>
					<ol class="locked-list">
					<% for (EventEditBean.Question q : ev.getQuestions()) { %>
						<li>
							<%= h(q.getQuestionText()) %>
							<small>（<%= q.isSelect() ? "選択式：" + h(q.getOptions()) : "自由記述" %><%= q.isRequired() ? "・必須" : "" %>）</small>
						</li>
					<% } %>
					</ol>
				<% } %>

			<% } else { %>
				<div id="questionList">
				<% for (EventEditBean.Question q : ev.getQuestions()) { %>
					<div class="question-block">
						<div class="question-head">
							<span class="question-number">質問</span>
							<button type="button" class="delete-button">削除</button>
						</div>

						<input type="text" data-name="text" maxlength="500" required
						       placeholder="例：今日いちばん話したいテーマは？" aria-label="質問文"
						       value="<%= h(q.getQuestionText()) %>">

						<div class="question-type">
							<span class="mini-label">回答形式</span>
							<label><input type="radio" data-name="type" value="select" <%= q.isSelect() ? "checked" : "" %>> 選択式</label>
							<label><input type="radio" data-name="type" value="text" <%= q.isSelect() ? "" : "checked" %>> 自由記述</label>
						</div>

						<div class="choices-row">
							<span class="mini-label">選択肢</span>
							<input type="text" data-name="options" maxlength="500"
							       placeholder="「／」で区切る　例：仕事・キャリア／趣味／地元の話" aria-label="選択肢"
							       value="<%= h(q.getOptions()) %>">
						</div>

						<label class="required-check">
							<input type="checkbox" data-name="required" value="true" <%= q.isRequired() ? "checked" : "" %>> 回答必須
						</label>
					</div>
				<% } %>
				</div>

				<button type="button" class="add-question-button" id="addQuestionButton">
					＋ 質問を追加（<span id="questionCounter">0</span> / 5）
				</button>

				<input type="hidden" name="questionCount" id="questionCount" value="0">
			<% } %>

				<p class="card-note">回答はアンケート結果画面で集計・確認できます。</p>
			</section>

		</div>

		<!-- キャンセル・保存 -->
		<div class="form-actions">
			<button type="button" class="cancel-button" id="cancelButton">キャンセル</button>
			<button type="submit" name="saveButton" class="create-button">変更を保存</button>
		</div>

	</form>

</main>

<!-- 質問ブロックのひな形（JS でコピーして使う） -->
<template id="questionTemplate">
	<div class="question-block">
		<div class="question-head">
			<span class="question-number">質問</span>
			<button type="button" class="delete-button">削除</button>
		</div>

		<input type="text" data-name="text" maxlength="500" required
		       placeholder="例：今日いちばん話したいテーマは？" aria-label="質問文">

		<div class="question-type">
			<span class="mini-label">回答形式</span>
			<label><input type="radio" data-name="type" value="select" checked> 選択式</label>
			<label><input type="radio" data-name="type" value="text"> 自由記述</label>
		</div>

		<div class="choices-row">
			<span class="mini-label">選択肢</span>
			<input type="text" data-name="options" maxlength="500"
			       placeholder="「／」で区切る　例：仕事・キャリア／趣味／地元の話" aria-label="選択肢">
		</div>

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

	/* ---------- 資料（参照ボタン） ---------- */
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

	/* ---------- 興味タグの追加 ---------- */
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
		cb.name = "newInterests";
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
		if (e.key === "Enter") { e.preventDefault(); addTag(); }
	});

	/* ---------- 参加者への質問（回答があって変更できないときは何もしない） ---------- */
	const questionList = document.getElementById("questionList");
	if (questionList) {
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
			const checked = block.querySelector('[data-name="type"]:checked');
			const isSelect = checked && checked.value === "select";
			const row = block.querySelector(".choices-row");
			row.hidden = !isSelect;
			row.querySelector("input").required = isSelect;
		}

		// 1つの質問ブロックにボタンの動きを付ける（登録済み・新規の両方で使う）
		function setupBlock(block) {
			block.querySelectorAll('[data-name="type"]').forEach(function (radio) {
				radio.addEventListener("change", function () { updateChoices(block); });
			});
			block.querySelector(".delete-button").addEventListener("click", function () {
				block.remove();
				renumber();
			});
			updateChoices(block);
		}

		addQuestionButton.addEventListener("click", function () {
			if (questionList.children.length >= MAX_QUESTIONS) return;
			const block = template.content.firstElementChild.cloneNode(true);
			questionList.appendChild(block);
			setupBlock(block);
			renumber();
		});

		// 登録済みの質問
		questionList.querySelectorAll(".question-block").forEach(setupBlock);
		renumber();
	}

	/* 入力欄の名前（エラーメッセージ用） */
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

		const deleting = form.querySelectorAll('input[name="deleteMaterialIds"]:checked').length;
		const msg = deleting > 0
			? "変更を保存しますか？（資料 " + deleting + " 件を削除します）"
			: "変更を保存しますか？";
		if (!confirm(msg)) {
			e.preventDefault();
		}
	});

	/* ---------- キャンセル ---------- */
	document.getElementById("cancelButton").addEventListener("click", function () {
		if (confirm("変更を破棄して、イベント管理画面に戻りますか？")) {
			location.href = "<%= ctx %>/admin/EventDashboard.action?eventId=<%= ev.getEventId() %>";
		}
	});
})();
</script>
