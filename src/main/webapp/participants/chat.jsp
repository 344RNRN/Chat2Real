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
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>チャット | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/chat.css?v=3">
    <style>
        /* ===== AI パネル：横に出しっぱなしにせず、押したときだけ右から出す ===== */
        .gemini-panel.ai-drawer {
            position: fixed;
            top: 0;
            right: 0;
            bottom: 0;
            z-index: 1000;

            width: min(380px, 92vw);
            max-width: none;
            margin: 0;
            overflow-y: auto;

            background: #ffffff;
            box-shadow: -12px 0 32px rgba(40, 66, 105, 0.18);

            transform: translateX(105%);
            visibility: hidden;
            transition: transform 0.25s ease, visibility 0s linear 0.25s;
        }

        .gemini-panel.ai-drawer.is-open {
            transform: translateX(0);
            visibility: visible;
            transition: transform 0.25s ease;
        }

        .ai-backdrop {
            position: fixed;
            inset: 0;
            z-index: 999;
            background: rgba(23, 59, 99, 0.25);
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.25s ease;
        }

        .ai-backdrop.is-open {
            opacity: 1;
            pointer-events: auto;
        }

        @media (prefers-reduced-motion: reduce) {
            .gemini-panel.ai-drawer,
            .ai-backdrop {
                transition: none;
            }
        }

        /* ===== 左のチャット一覧：<a> にしたので文字色・下線をそろえる ===== */
        a.conversation-item {
            display: flex;
            color: inherit;
            text-decoration: none;
        }

        a.conversation-item[hidden] {
            display: none;
        }

        .conversation-info p {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        /* ===== まだだれともチャットしていないとき ===== */
        .chat-empty-page {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 48px 16px;
        }

        .chat-empty-card {
            width: min(100%, 460px);
            padding: 40px 28px;

            text-align: center;
            background: #ffffff;
            border: 1px solid #dce5f1;
            border-radius: 22px;
            box-shadow: 0 18px 50px rgba(40, 66, 105, 0.08);
        }

        .chat-empty-icon {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 72px;
            height: 72px;
            font-size: 32px;
            background: #fff0f6;
            border-radius: 50%;
        }

        .chat-empty-card h1 {
            margin: 16px 0 8px;
            color: #173b63;
            font-size: 20px;
        }

        .chat-empty-card p {
            margin: 0 0 24px;
            color: #7184a0;
            font-size: 14px;
            line-height: 1.8;
        }

        .chat-empty-button {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 48px;
            padding: 0 28px;

            color: #ffffff;
            background: linear-gradient(120deg, #ff7eaa 0%, #f44780 100%);
            border-radius: 11px;
            box-shadow: 0 8px 18px rgba(244, 91, 145, 0.22);

            font-weight: 800;
            text-decoration: none;
        }
    </style>
</head>
<body>

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
    <aside class="chat-list-panel">

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

            <button type="button" class="chat-back"
                    onclick="history.back()" aria-label="戻る">
                ←
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
    backdrop.addEventListener("click", closeAi);
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") closeAi();
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

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
