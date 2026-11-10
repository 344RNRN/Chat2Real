<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.ArrayList, java.util.List" %>
<%@ page import="Bean.ChatPartnerBean, Bean.ChatMessageBean, Bean.ChatConversationBean" %>
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
        return s == null || s.trim().isEmpty();
    }
%>
<%
    String ctx = request.getContextPath();

    // ===== サーブレット（ChatAction）から受け取るデータ =====
    ChatPartnerBean partner = (ChatPartnerBean) request.getAttribute("partner");

    // partner が無い ＝ まだだれともチャットしていない（参加者一覧の「チャット画面へ」から開いたとき）
    //   → 下で「チャットする相手を選んでください」を表示する
    boolean noPartner = (partner == null);

    @SuppressWarnings("unchecked")
    List<ChatMessageBean> messages = (List<ChatMessageBean>) request.getAttribute("messages");
    @SuppressWarnings("unchecked")
    List<String> commonTags = (List<String>) request.getAttribute("commonTags");
    String startedLabel = (String) request.getAttribute("startedLabel");
    String flashMessage = (String) request.getAttribute("flashMessage");
    Long chatId = (Long) request.getAttribute("chatId");

    @SuppressWarnings("unchecked")
    List<ChatConversationBean> conversations = (List<ChatConversationBean>) request.getAttribute("conversations");

    if (messages == null) messages = new ArrayList<>();
    if (conversations == null) conversations = new ArrayList<>();
    if (commonTags == null) commonTags = new ArrayList<>();

    long partnerId = noPartner ? 0L : partner.getId();   // = 相手の event_participant_id
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover, interactive-widget=resizes-content">
    <title>チャット | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/chat.css?v=8">
</head>
<body class="chat-screen">

<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>

<% if (noPartner) { %>
<!-- まだだれともチャットしていないとき -->
<main class="chat-empty-page">
    <div class="chat-empty-card">
        <span class="chat-empty-icon" aria-hidden="true">💬</span>
        <h1>チャットする相手を選んでください</h1>
        <p>まだだれともチャットしていません。<br>参加者一覧で気になる人の「チャットする」を押すと、ここで会話できます。</p>
        <a href="<%= ctx %>/participant/ParticipantList.action" class="chat-empty-button">参加者一覧へ</a>
    </div>
</main>
<% } else { %>
<div class="chat-layout">

    <!-- 01. LEFT NAVIGATION（実在する画面にリンク） -->
    <aside class="app-sidebar">
        <div class="brand">
            <span class="brand-icon">♡</span>
            <span>Chat2Real</span>
        </div>

        <nav class="side-nav">
            
            <a href="<%= ctx %>/participant/ParticipantList.action">
                <span>♧</span> 参加者一覧
            </a>
            <a href="<%= ctx %>/chat?partnerId=<%= partnerId %>" class="active" aria-current="page">
                <span>▢</span> チャット
            </a>
            
        </nav>

        <div class="sidebar-bottom">
            <p>人と人をつなぐ、</p>
            <p>新しい出会い。</p>
            <span>Chat2Real</span>
        </div>
    </aside>


    <!-- 02. CONVERSATION LIST -->
    <aside class="chat-list-panel" id="chatListPanel">

        <div class="list-heading">
            <h2>チャット</h2>
        </div>

        <div class="search-box">
            <span>⌕</span>
            <input type="search" id="chatSearch"
                   placeholder="名前で検索...">
        </div>

        <div class="chat-filters">
            <button class="filter active" type="button"
                    data-filter="all">すべて</button>
            <button class="filter" type="button"
                    data-filter="ongoing">やりとり中</button>
            <button class="filter" type="button"
                    data-filter="ended">終了</button>
        </div>

        <div class="conversation-list" id="conversationList">

            <%-- 自分がチャットしたことのある相手 全員（新しいメッセージ順）。押すとその人とのチャットへ --%>
        <% boolean currentListed = false;
           for (ChatConversationBean cv : conversations) {
               boolean selected = (cv.getPartnerId() == partnerId);
               if (selected) currentListed = true; %>
            <a class="conversation-item<%= selected ? " selected" : "" %>"
               href="<%= ctx %>/chat?partnerId=<%= cv.getPartnerId() %>"
               data-status="<%= cv.isEnded() ? "ended" : "ongoing" %>"
               data-name="<%= h(cv.getDisplayName()) %>"
               <%= selected ? "aria-current=\"page\"" : "" %>>

                <div class="conversation-avatar">
                <% if (!isBlank(cv.getPhotoUrl())) { %>
                    <img src="<%= ctx %><%= h(cv.getPhotoUrl()) %>" alt="">
                <% } else { %>
                    <span>♙</span>
                <% } %>
                </div>

                <div class="conversation-info">
                    <div class="conversation-top">
                        <strong><%= h(cv.getDisplayName()) %></strong>
                        <span class="conversation-time"><%= h(cv.getTimeText()) %></span>
                        <span class="cn-badge" data-unread-partner="<%= cv.getPartnerId() %>"<%= cv.getUnreadCount() > 0 ? "" : " hidden" %>><%= cv.getUnreadCount() %></span>
                    </div>
                    <p><%= cv.isEnded() ? "【終了】" : "" %><%= h(cv.getPreview()) %></p>
                </div>
            </a>
        <% } %>

        <% if (!currentListed) { %>
            <%-- 一覧が取れなかったとき（t014_chat_participants が無い など）は今の相手だけ出す --%>
            <div class="conversation-item selected"
                 data-status="ongoing"
                 data-name="<%= h(partner.getDisplayName()) %>">
                <div class="conversation-avatar">
                <% if (!isBlank(partner.getPhotoUrl())) { %>
                    <img src="<%= ctx %><%= h(partner.getPhotoUrl()) %>" alt="">
                <% } else { %>
                    <span>♙</span>
                <% } %>
                </div>
                <div class="conversation-info">
                    <div class="conversation-top">
                        <strong><%= h(partner.getDisplayName()) %></strong>
                        <span class="conversation-time"> </span>
                    </div>
                    <p>会話を開始しました</p>
                </div>
            </div>
        <% } %>

            <div class="empty-conversations" hidden>
                会話が見つかりません。
            </div>
        </div>
    </aside>


    <!-- 03. MAIN CHAT -->
    <main class="chat-main">

        <header class="chat-head">

            <a class="chat-back" href="<%= ctx %>/participant/ParticipantList.action" aria-label="参加者一覧に戻る">
                ←
            </a>

            <button type="button" class="chat-list-toggle" id="chatListToggle"
                    aria-label="チャット一覧" aria-controls="chatListPanel" aria-expanded="false">
                ☰
                <span class="cn-badge" id="chatUnreadBadge" hidden>0</span>
            </button>

            <a class="chat-avatar" href="<%= ctx %>/participant/AccountProfile.action?id=<%= partnerId %>"
               title="<%= h(partner.getDisplayName()) %>さんのプロフィール">
            <% if (!isBlank(partner.getPhotoUrl())) { %>
                <img src="<%= ctx %><%= h(partner.getPhotoUrl()) %>" alt="">
            <% } else { %>
                ♙
            <% } %>
            </a>

            <div class="chat-who">
                <h1><%= h(partner.getDisplayName()) %></h1>
                <p>Chat2Realで会話中</p>
            </div>

            <div class="chat-head-acts">
                <span class="online-status">オンライン</span>

                <%-- ※交流申請の画面ができたら action を合わせる --%>
                <form method="get" action="<%= ctx %>/interaction/request">
                    <input type="hidden" name="partnerId" value="<%= partnerId %>">
                    <button type="submit" class="req">申請</button>
                </form>

                <form method="post" action="<%= ctx %>/chat/end"
                      onsubmit="return confirm('チャットを終了しますか？');">
                    <input type="hidden" name="partnerId" value="<%= partnerId %>">
                    <button type="submit" class="end">終了</button>
                </form>
            </div>
        </header>


        <section class="chat-body" id="messages">

            <p class="chat-date">
                <span><%= h(startedLabel) %></span>
            </p>

        <% if (flashMessage != null) { %>
            <p class="chat-date"><span><%= h(flashMessage) %></span></p>
        <% } %>

            <div id="messageList">
            <% for (ChatMessageBean m : messages) { %>
                <div class="msg <%= m.isMine() ? "me" : "other" %>"><p><%= h(m.getText()) %></p><span class="meta"><%= h(m.getTime()) %></span></div>
            <% } %>
            </div>

            <!-- COMMON INTERESTS -->
        <% if (!commonTags.isEmpty()) { %>
            <div class="common-interests">
                <strong>共通の興味・特徴</strong>
                <div class="interest-tags">
                <% for (String tag : commonTags) { %>
                    <span class="interest-tag"><%= h(tag) %></span>
                <% } %>
                </div>
            </div>
        <% } %>

        </section>


        <!-- MESSAGE INPUT -->
        <form id="sendForm" class="chat-send"
              method="post" action="<%= ctx %>/chat/send">

            <input type="hidden" name="partnerId" value="<%= partnerId %>">

            <input type="text" name="message" id="message"
                   maxlength="200"
                   placeholder="メッセージを入力..."
                   autocomplete="off" required>

            <button type="button" class="ai-open"
                    id="openAiButton" title="Gemini AI">
                ✨<small>AI</small>
            </button>

            <button type="submit" class="send-button"
                    name="sendButton" aria-label="送信">
                ➤
            </button>
        </form>

    </main>


    <!-- 04. GEMINI AI PANEL -->
    <aside class="gemini-panel ai-drawer" id="geminiPanel" aria-hidden="true">

        <div class="gemini-heading">
            <div>
                <span class="gemini-icon">✦</span>
                <strong>Gemini AI チャットヘルプ</strong>
                <span class="beta-label">Beta</span>
            </div>

            <button type="button" id="closeAiButton"
                    aria-label="閉じる">×</button>
        </div>

        <p class="gemini-description">
            これまでの会話を参考に、次のメッセージを提案します。
        </p>

        <!-- CONVERSATION SUMMARY -->
        <section class="ai-summary">
            <h3>☏ 会話の要点（AIが理解した内容）</h3>
            <ul>
                <li>イベントで知り合った</li>
                <li>共通の趣味について会話中</li>
                <li>お互いの興味を共有している</li>
            </ul>
        </section>

        <!-- AI SUGGESTIONS -->
        <section class="ai-suggestions">
            <h3>✦ おすすめの返信（3つ）</h3>

            <div class="suggestion-card">
                <p>最近おすすめのアニメはありますか？</p>
                <button type="button" class="copy-suggestion">
                    ▢ コピー
                </button>
            </div>

            <div class="suggestion-card">
                <p>他にも好きな趣味はありますか？</p>
                <button type="button" class="copy-suggestion">
                    ▢ コピー
                </button>
            </div>

            <div class="suggestion-card">
                <p>このイベントに参加したきっかけは何ですか？</p>
                <button type="button" class="copy-suggestion">
                    ▢ コピー
                </button>
            </div>
        </section>

        <div class="ai-actions">
            <button type="button" id="regenerateButton">
                ↻ 別の提案を生成
            </button>

            <select id="aiTone" aria-label="返信のトーン">
                <option value="casual">カジュアル</option>
                <option value="polite">丁寧</option>
                <option value="friendly">親しみやすい</option>
            </select>
        </div>

        <div class="ai-hint">
            <strong>💡 使い方のヒント</strong>
            <p>・提案を「コピー」で入力欄に入れられます。</p>
            <p>・トーンを変更して返信を調整できます。</p>
            <p>・AIの提案は自由に編集して送信できます。</p>
        </div>

    </aside>

</div>

<!-- AI パネルの後ろの暗い部分（押すと閉じる） -->
<div class="ai-backdrop" id="aiBackdrop"></div>

<script>
(function () {
    const MESSAGES_URL = "<%= ctx %>/chat/messages?partnerId=<%= partnerId %>";
    const ENDED_URL = "<%= ctx %>/participant/ChatEnd.action?chatId=<%= chatId == null ? 0 : chatId %>";

    const body = document.getElementById("messages");
    const list = document.getElementById("messageList");
    const input = document.getElementById("message");
    const form = document.getElementById("sendForm");

    function scrollBottom() {
        body.scrollTop = body.scrollHeight;
    }

    scrollBottom();
    // ===== スマホ：画面に「見えている」高さに合わせる =====
    // iPhone の Safari は、下のツールバーやキーボードの後ろまで画面の高さに数えるので、
    // そのままだと入力欄が隠れる。見えている高さ（visualViewport）を --app-h に入れて、その高さにする
    const vv = window.visualViewport;
    const root = document.documentElement;

    // iPhone・iPad：画面の下に浮いているツールバー（＜・共有・更新）の分、入力欄を上げる（CSS の .is-ios）
    const isIOS = /iPhone|iPad|iPod/.test(navigator.userAgent)
            || (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);
    if (isIOS) root.classList.add("is-ios");

    let fullHeight = window.innerHeight;
    const sendBar = document.getElementById("sendForm");
    const phone = window.matchMedia("(max-width: 900px)");

    // iPhone の下に浮いているツールバー（＜・共有・更新）に入力欄が隠れるときだけ、ここを 70 くらいにする
    // （ふつうは 0 のままでよい：見えている高さに合わせているので、ツールバーの上で止まる）
    const IOS_TOOLBAR_SPACE = 120;

    const chatLayoutEl = document.querySelector(".chat-layout");
    const chatMainEl = document.querySelector(".chat-main");
    const siteHeaderEl = document.querySelector("body.chat-screen > header, body.chat-screen .header");

    function fitViewport() {
        const h = vv ? vv.height : window.innerHeight;
        const top = vv ? vv.offsetTop : 0;
        root.style.setProperty("--app-h", h + "px");
        root.style.setProperty("--app-top", top + "px");

        // キーボードが出ている（見えている高さが大きく減った）ときは、ツールバー分の余白はいらない
        fullHeight = Math.max(fullHeight, window.innerHeight);
        const keyboard = h < fullHeight - 120;
        root.classList.toggle("is-keyboard", keyboard);

        // ★ chat.css が古いまま（スマホのキャッシュ）でも効くように、ここで直接指定する
        const b = document.body.style;
        const l = chatLayoutEl.style;
        const m = chatMainEl.style;
        if (phone.matches) {
            // 画面（見えている部分）＝ ヘッダー ＋ チャット。チャットは残りの高さぴったり
            b.position = "fixed";
            b.left = "0";
            b.right = "0";
            b.top = top + "px";
            b.height = h + "px";
            b.display = "flex";
            b.flexDirection = "column";
            b.overflow = "hidden";

            if (siteHeaderEl) siteHeaderEl.style.flexShrink = "0";
            document.querySelectorAll("body.chat-screen .footer, body.chat-screen > footer")
                .forEach(function (f) { f.style.display = "none"; });

            l.display = "flex";
            l.flexDirection = "column";
            l.flex = "1 1 auto";
            l.height = "auto";
            l.minHeight = "0";
            l.padding = "0";
            l.gap = "0";

            m.flex = "1 1 auto";
            m.height = "auto";
            m.minHeight = "0";

            sendBar.style.flexShrink = "0";
            sendBar.style.paddingBottom = (IOS_TOOLBAR_SPACE > 0 && isIOS && !keyboard)
                    ? IOS_TOOLBAR_SPACE + "px" : "";
        } else {
            ["position", "left", "right", "top", "height", "display", "flexDirection", "overflow"]
                .forEach(function (k) { b[k] = ""; });
            ["display", "flexDirection", "flex", "height", "minHeight", "padding", "gap"]
                .forEach(function (k) { l[k] = ""; });
            ["flex", "height", "minHeight"].forEach(function (k) { m[k] = ""; });
            sendBar.style.paddingBottom = "";
        }

        scrollBottom();
    }
    fitViewport();
    window.addEventListener("load", fitViewport);
    window.addEventListener("resize", fitViewport);
    window.addEventListener("orientationchange", fitViewport);
    if (vv) {
        vv.addEventListener("resize", fitViewport);   // キーボードが出た・消えた
        vv.addEventListener("scroll", fitViewport);
    }
    // 入力欄を押したとき、ページ全体がずれないようにする
    input.addEventListener("focus", function () {
        setTimeout(function () {
            window.scrollTo(0, 0);
            fitViewport();
        }, 50);
    });

    form.addEventListener("submit", function (e) {
        if (!input.value.trim()) {
            e.preventDefault();
        }
    });

    // Gemini suggestion copy: copy into message input, not send automatically.
    document.querySelectorAll(".copy-suggestion").forEach(function (button) {
        button.addEventListener("click", function () {
            const text = button.parentElement.querySelector("p").textContent.trim();
            input.value = text;
            closeAi();
            input.focus();
        });
    });

    // ===== AI パネル：最初は隠しておき、✨AI ボタンを押したときだけ右から出す =====
    const panel = document.getElementById("geminiPanel");
    const backdrop = document.getElementById("aiBackdrop");
    const layout = document.querySelector(".chat-layout");

    // AI パネルが無くなった分、チャット欄を右いっぱいまで広げる（chat.css が grid のとき）
    function fitLayout() {
        layout.style.gridTemplateColumns = "";
        if (window.matchMedia("(max-width: 900px)").matches) return;   // スマホは CSS で縦並び
        const st = getComputedStyle(layout);
        if (st.display !== "grid") return;
        const cols = st.gridTemplateColumns.split(" ").filter(Boolean);
        if (cols.length >= 4) {
            layout.style.gridTemplateColumns = cols.slice(0, cols.length - 2).join(" ") + " minmax(0, 1fr)";
        }
    }
    fitLayout();
    window.addEventListener("resize", fitLayout);

    function openAi() {
        panel.classList.add("is-open");
        backdrop.classList.add("is-open");
        panel.setAttribute("aria-hidden", "false");
    }
    function closeAi() {
        panel.classList.remove("is-open");
        backdrop.classList.remove("is-open");
        panel.setAttribute("aria-hidden", "true");
    }

    document.getElementById("openAiButton").addEventListener("click", function () {
        panel.classList.contains("is-open") ? closeAi() : openAi();
    });
    document.getElementById("closeAiButton").addEventListener("click", closeAi);

    // ===== スマホ：☰ でチャット一覧を左から出す =====
    const listPanel = document.getElementById("chatListPanel");
    const listToggle = document.getElementById("chatListToggle");
    function closeList() {
        listPanel.classList.remove("is-open");
        listToggle.setAttribute("aria-expanded", "false");
        if (!panel.classList.contains("is-open")) backdrop.classList.remove("is-open");
    }
    listToggle.addEventListener("click", function () {
        const open = !listPanel.classList.contains("is-open");
        listPanel.classList.toggle("is-open", open);
        backdrop.classList.toggle("is-open", open);
        listToggle.setAttribute("aria-expanded", open ? "true" : "false");
    });

    backdrop.addEventListener("click", function () {
        closeAi();
        closeList();
    });
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            closeAi();
            closeList();
        }
    });

    // Conversation list search
    const search = document.getElementById("chatSearch");
    search.addEventListener("input", function () {
        const keyword = search.value.toLowerCase();
        document.querySelectorAll(".conversation-item").forEach(function (item) {
            item.hidden = !item.dataset.name.toLowerCase().includes(keyword);
        });
    });

    // Conversation filters
    document.querySelectorAll(".filter").forEach(function (button) {
        button.addEventListener("click", function () {
            document.querySelectorAll(".filter").forEach(function (b) {
                b.classList.remove("active");
            });
            button.classList.add("active");

            const filter = button.dataset.filter;
            document.querySelectorAll(".conversation-item").forEach(function (item) {
                item.hidden = filter !== "all" && item.dataset.status !== filter;
            });
        });
    });

    // 3秒ごとに新しいメッセージを読み込む（ChatAction の /chat/messages）
    let fails = 0;
    const timer = setInterval(function () {
        fetch(MESSAGES_URL, { credentials: "same-origin" })
        .then(function (response) {
            if (response.headers.get("X-Chat-Ended")) {   // 相手がチャットを終了した
                clearInterval(timer);
                location.href = ENDED_URL;
                return null;
            }
            if (!response.ok) throw new Error(response.status);
            return response.text();
        })
        .then(function (html) {
            if (html === null) return;
            fails = 0;

            const nearBottom =
                body.scrollHeight - body.scrollTop - body.clientHeight < 80;

            if (list.innerHTML.trim() !== html.trim()) {
                list.innerHTML = html;
                if (nearBottom) scrollBottom();
            }
        })
        .catch(function () {
            if (++fails >= 3) clearInterval(timer);
        });
    }, 3000);
})();
</script>
<% } %>

<%-- 新着メッセージ通知：いま話している相手の通知は出さない --%>
<script>window.CHAT_NOTIFY_IGNORE = "<%= partnerId > 0 ? String.valueOf(partnerId) : "" %>";</script>
<%@ include file="/common/chat-notify.jsp" %>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
