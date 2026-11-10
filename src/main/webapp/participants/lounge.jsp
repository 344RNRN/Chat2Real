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
%>
<%
    String ctx = request.getContextPath();

    // ===== サーブレット（LoungeAction）から受け取るデータ =====
    LoungeBean lounge = (LoungeBean) request.getAttribute("lounge");
    if (lounge == null) {
        // Action を通さずに開いたときは Action から開き直す
        response.sendRedirect(ctx + "/participant/Lounge.action");
        return;
    }
    String tab = "chat".equals(request.getAttribute("tab")) ? "chat" : "docs";
    String flash = (String) request.getAttribute("flash");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>イベント資料・全体チャット | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/lounge.css">
</head>
<body class="lounge-screen">

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<main class="lounge-page">

    <!-- 見出し -->
    <section class="lounge-heading">
        <div>
            <span class="page-eyebrow">MATERIALS &amp; CHAT</span>
            <h1>イベント資料・全体チャット</h1>
            <p class="page-description"><%= h(lounge.getEventName()) %></p>
        </div>

        <!-- 今の状態と「交流したい」への切り替え -->
        <form action="<%= ctx %>/participant/ChangeStatus.action" method="post" class="status-box">
            <span class="status-text">
            <% if (lounge.isWant()) { %>
                <span class="status-dot want"></span>いまは「交流したい」です
            <% } else { %>
                <span class="status-dot"></span>交流はお休み中
            <% } %>
            </span>
        <% if (!lounge.isWant()) { %>
            <button type="submit" name="wantButton" value="交流したい" class="want-button">
                <span aria-hidden="true">💬</span> 交流したい に切り替える
            </button>
        <% } else { %>
            <a href="<%= ctx %>/participant/ParticipantList.action" class="want-button">
                <span aria-hidden="true">🧑‍🤝‍🧑</span> 参加者一覧へ
            </a>
        <% } %>
        </form>
    </section>

    <!-- スマホ用タブ（PC では隠れて、2つ並べて表示） -->
    <div class="lounge-tabs" role="tablist" aria-label="表示の切り替え">
        <button type="button" role="tab" class="lounge-tab<%= "docs".equals(tab) ? " is-active" : "" %>"
                data-tab="docs" aria-controls="docsPanel" aria-selected="<%= "docs".equals(tab) %>">
            <span aria-hidden="true">📄</span> 資料
            <span class="tab-count"><%= lounge.getItems().size() %></span>
        </button>
        <button type="button" role="tab" class="lounge-tab<%= "chat".equals(tab) ? " is-active" : "" %>"
                data-tab="chat" aria-controls="chatPanel" aria-selected="<%= "chat".equals(tab) %>">
            <span aria-hidden="true">👥</span> 全体チャット
        </button>
    </div>

    <div class="lounge-grid" data-tab="<%= tab %>">

        <!-- 左：お知らせ・資料 -->
        <section class="panel docs-panel" id="docsPanel" role="tabpanel">
            <h2 class="panel-title"><span aria-hidden="true">📄</span> お知らせ・資料</h2>

        <% if (lounge.getItems().isEmpty()) { %>
            <p class="panel-empty">まだお知らせ・資料はありません。</p>
        <% } else { %>
            <ul class="doc-list">
            <% for (LoungeBean.Item it : lounge.getItems()) {
                   String kind = it.getKindLabel(); %>
                <li class="doc-item">
                    <span class="doc-icon kind-<%= it.isNotice() ? "notice" : "file" %>"><%= h(kind) %></span>
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
                        <span class="doc-meta">運営者からのお知らせ<%= it.getTimeText() == null || it.getTimeText().isEmpty() ? "" : "・" + h(it.getTimeText()) %></span>
                    <% } else { %>
                        <a class="doc-title doc-link" href="<%= ctx %><%= h(it.getFilePath()) %>" target="_blank" rel="noopener">
                            <%= h(it.getTitle()) %>
                        </a>
                        <span class="doc-meta">運営者<%= it.getTimeText() == null || it.getTimeText().isEmpty() ? "" : "・" + h(it.getTimeText()) %></span>
                    <% } %>
                    </div>
                </li>
            <% } %>
            </ul>
        <% } %>
        </section>

        <!-- 右：全体チャット -->
        <section class="panel chat-panel" id="chatPanel" role="tabpanel">
            <h2 class="panel-title">
                <span aria-hidden="true">👥</span> 全体チャット
                <span class="member-count">（<%= lounge.getMemberCount() %>名）</span>
            </h2>

            <div class="message-list" id="messageList" aria-live="polite">
            <% if (lounge.getMessages().isEmpty()) { %>
                <p class="chat-empty">まだメッセージはありません。最初のひとことを送ってみましょう！</p>
            <% } %>
            <% for (LoungeBean.Message m : lounge.getMessages()) { %>
                <div class="msg<%= m.isMine() ? " mine" : "" %>">
                <% if (!m.isMine()) { %>
                    <span class="msg-name"><%= h(m.getNickname()) %></span>
                <% } %>
                    <p class="msg-text"><%= h(m.getText()) %></p>
                    <span class="msg-time"><%= h(m.getTimeText()) %></span>
                </div>
            <% } %>
            </div>

        <% if (flash != null) { %>
            <p class="chat-flash" role="alert"><%= h(flash) %></p>
        <% } %>

            <form action="<%= ctx %>/participant/Lounge.action" method="post" class="chat-form" id="chatForm">
                <label for="messageInput" class="visually-hidden">メッセージ</label>
                <textarea id="messageInput" name="message" rows="1" maxlength="500"
                          placeholder="メッセージを入力" required></textarea>
                <button type="submit" class="send-button">送信</button>
            </form>
            <p class="chat-hint">Ctrl + Enter でも送信できます</p>
        </section>

    </div>

    <!-- 下のリンク -->
    <nav class="lounge-links">
        <a href="<%= ctx %>/participant/Top.action" class="room-link">‹ イベントルームに戻る</a>
    </nav>

</main>

<script>
(function () {
    var grid = document.querySelector(".lounge-grid");
    var tabs = document.querySelectorAll(".lounge-tab");
    var list = document.getElementById("messageList");
    var input = document.getElementById("messageInput");
    var form = document.getElementById("chatForm");

    // ---- タブ切り替え（スマホ）----
    function showTab(name) {
        grid.setAttribute("data-tab", name);
        tabs.forEach(function (t) {
            var on = t.getAttribute("data-tab") === name;
            t.classList.toggle("is-active", on);
            t.setAttribute("aria-selected", on ? "true" : "false");
        });
        try {
            var url = new URL(location.href);
            url.searchParams.set("tab", name);
            history.replaceState(null, "", url);
        } catch (e) { /* 古いブラウザは URL を変えない */ }
        if (name === "chat") {
            scrollToBottom();
            showChatOnPhone();
        }
    }

    // スマホ：チャット欄（入力欄まで）が画面に入るようにスクロール
    function showChatOnPhone() {
        if (window.matchMedia("(max-width: 900px)").matches) {
            document.getElementById("chatPanel").scrollIntoView({ block: "start" });
        }
    }
    tabs.forEach(function (t) {
        t.addEventListener("click", function () { showTab(t.getAttribute("data-tab")); });
    });

    // ---- メッセージ ----
    function scrollToBottom() {
        list.scrollTop = list.scrollHeight;
    }
    function nearBottom() {
        return list.scrollHeight - list.scrollTop - list.clientHeight < 80;
    }
    scrollToBottom();
    if (grid.getAttribute("data-tab") === "chat") {
        showChatOnPhone();          // 送信したあと・チャットタブで開いたとき
    }

    // 入力欄：内容に合わせて高さを伸ばす・Ctrl/⌘ + Enter で送信
    input.addEventListener("input", function () {
        input.style.height = "auto";
        input.style.height = Math.min(input.scrollHeight, 120) + "px";
    });
    input.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) {
            e.preventDefault();
            if (input.value.trim() !== "") {
                form.submit();
            }
        }
    });

    // 4秒ごとに新しいメッセージを読み込む（入力中の文字は消えない）
    var last = list.innerHTML.trim();
    setInterval(function () {
        if (document.hidden) {
            return;
        }
        fetch("<%= ctx %>/participant/Lounge.action?part=messages", { cache: "no-store" })
            .then(function (res) { return res.ok ? res.text() : null; })
            .then(function (html) {
                if (html === null || html.trim() === last) {
                    return;
                }
                var stick = nearBottom();
                list.innerHTML = html;
                last = html.trim();
                if (stick) {
                    scrollToBottom();
                }
            })
            .catch(function () { /* 通信できないときは次の回にまた試す */ });
    }, 4000);
})();
</script>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
