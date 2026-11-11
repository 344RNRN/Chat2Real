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

    /** JavaScript の文字列（"…"）として安全に出す */
    private static String js(Object value) {
        if (value == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toString().toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': break;
                case '<':  sb.append("\\u003c"); break;
                case '>':  sb.append("\\u003e"); break;
                case '&':  sb.append("\\u0026"); break;
                default:   sb.append(c);
            }
        }
        return sb.append('"').toString();
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
    <link rel="stylesheet" href="<%= ctx %>/css/chat.css?v=11">
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

                <form method="post"
      				action="<%= ctx %>/participant/ChatEnd.action"
      				onsubmit="return confirm('チャットを終了しますか？');">

    					<input type="hidden"
           					name="chatId"
           					value="<%= chatId == null ? 0 : chatId %>">

    					<button type="submit" class="end">終了</button>
				</form>
            </div>
        </header>

        <!-- 交流申請のお知らせ（相手から届いた・相互承認が成立した） -->
        <a class="request-banner" id="requestBanner" href="<%= ctx %>/interaction/request?partnerId=<%= partnerId %>" hidden>
            <span class="request-banner-icon" aria-hidden="true">💌</span>
            <span class="request-banner-text" id="requestBannerText"></span>
            <span class="request-banner-go">確認する ›</span>
        </a>


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
            <ul id="aiSummary">
                <li>イベントで知り合った</li>
            </ul>
        </section>

        <!-- AI SUGGESTIONS -->
        <section class="ai-suggestions">
            <h3>✦ おすすめの返信（3つ）</h3>
            <div id="aiSuggestionList">
                <%-- 下の JavaScript が会話の内容から作る --%>
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
<<<<<<< HEAD
    const IOS_TOOLBAR_SPACE = 200;
=======
    const IOS_TOOLBAR_SPACE = 120;
>>>>>>> branch 'main' of https://github.com/344RNRN/Chat2Real.git

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

    // =====================================================================
    // AI チャットヘルプ：会話の内容（共通の興味・相手の最後のメッセージ）から返信を提案する
    //   「別の提案を生成」を押すたびに別の3つ。トーン（カジュアル／丁寧／親しみやすい）でも変わる
    // =====================================================================
    const AI_PARTNER = <%= js(partner.getDisplayName()) %>;
    const AI_TAGS = [<% for (int i = 0; i < commonTags.size(); i++) { %><%= i > 0 ? "," : "" %><%= js(commonTags.get(i)) %><% } %>];

    const suggestionList = document.getElementById("aiSuggestionList");
    const summaryList = document.getElementById("aiSummary");
    const toneSelect = document.getElementById("aiTone");
    const regenButton = document.getElementById("regenerateButton");
    let lastShown = [];

    // トーンごとの言い方（{tag} は共通の興味、{last} は相手の最後のメッセージの一部）
    const AI_TEMPLATES = {
        casual: {
            tag: [
                "{tag}好きなんだね！最近ハマってるのある？",
                "{tag}の話もっと聞きたい！おすすめある？",
                "{tag}って、いつから好きなの？",
                "今度{tag}の話でゆっくり盛り上がろう！",
                "{tag}だと、どんなのが一番好き？"
            ],
            reply: [
                "「{last}」いいね！もうちょっと詳しく教えて！",
                "それわかる！自分も「{last}」気になってた",
                "「{last}」って、どういうところが好き？"
            ],
            question: [
                "うーん、自分はまだ迷ってるかも。そっちはどう？",
                "いい質問！ちょっと考えるね。先に教えて！"
            ],
            general: [
                "今日のイベント、どこが一番楽しかった？",
                "このイベントに来たきっかけって何？",
                "休みの日って何してることが多い？",
                "最近ハマってることある？",
                "このあと会場で少し話せたらうれしいな！"
            ]
        },
        polite: {
            tag: [
                "{tag}がお好きなんですね！最近のおすすめはありますか？",
                "{tag}について、ぜひもう少しお話を聞かせてください。",
                "{tag}はいつ頃からお好きなんですか？",
                "{tag}の中で、特にお気に入りのものはありますか？"
            ],
            reply: [
                "「{last}」とのこと、とても興味があります。詳しく教えていただけますか？",
                "「{last}」、素敵ですね。どんなところが魅力ですか？"
            ],
            question: [
                "ご質問ありがとうございます。少し考えてからお答えしますね。",
                "私はまだ迷っているのですが、{name}さんはいかがですか？"
            ],
            general: [
                "今日のイベントで印象に残ったことはありますか？",
                "このイベントに参加されたきっかけは何ですか？",
                "お休みの日はどのように過ごされていますか？",
                "よろしければ、このあと会場で少しお話しできませんか？"
            ]
        },
        friendly: {
            tag: [
                "{tag}好き仲間だね😊 最近のおすすめ教えて〜！",
                "{tag}の話できる人いてうれしい！どんなのが好き？",
                "{tag}つながりで仲良くなれそう！今度語ろう〜",
                "{tag}のこと、もっと知りたいな✨"
            ],
            reply: [
                "「{last}」めっちゃいいね！それでそれで？😊",
                "「{last}」わかる〜！もっと聞かせて！"
            ],
            question: [
                "えー迷う〜！そっちはどう思う？😊",
                "いい質問！先に答え聞いてもいい？笑"
            ],
            general: [
                "今日来てよかった〜！そっちは楽しめてる？",
                "どうしてこのイベントに来たの？気になる〜",
                "せっかくだから、あとで会場でもお話ししよう！😊",
                "最近楽しかったこと教えて〜！"
            ]
        }
    };

    function shuffle(arr) {
        const a = arr.slice();
        for (let i = a.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [a[i], a[j]] = [a[j], a[i]];
        }
        return a;
    }

    /** 今画面に出ているメッセージから、相手の最後のメッセージと件数を取る */
    function readConversation() {
        const others = document.querySelectorAll("#messageList .msg.other p");
        const mine = document.querySelectorAll("#messageList .msg.me p");
        const last = others.length ? others[others.length - 1].textContent.trim() : "";
        return { last: last, otherCount: others.length, myCount: mine.length };
    }

    /** 候補をたくさん作って、前回と違う3つを選ぶ */
    function buildSuggestions() {
        const t = AI_TEMPLATES[toneSelect.value] || AI_TEMPLATES.casual;
        const conv = readConversation();
        const shortLast = conv.last.length > 14 ? conv.last.slice(0, 14) + "…" : conv.last;
        const pool = [];

        if (conv.last) {
            const isQuestion = /[?？]$|か[?？]?$/.test(conv.last);
            (isQuestion ? t.question : t.reply).forEach(function (x) {
                pool.push({ text: x.replace("{last}", shortLast), kind: "reply" });
            });
        }
        AI_TAGS.forEach(function (tag) {
            t.tag.forEach(function (x) { pool.push({ text: x.split("{tag}").join(tag), kind: "tag" }); });
        });
        t.general.forEach(function (x) { pool.push({ text: x, kind: "general" }); });

        // 前回出したものは後回し
        const fresh = shuffle(pool.filter(function (p) { return lastShown.indexOf(p.text) < 0; }));
        const used = shuffle(pool.filter(function (p) { return lastShown.indexOf(p.text) >= 0; }));
        const ordered = fresh.concat(used);

        // できるだけ「返事・共通の興味・一般」をばらけさせる
        const picked = [];
        ["reply", "tag", "general"].forEach(function (k) {
            const hit = ordered.find(function (p) { return p.kind === k && picked.indexOf(p) < 0; });
            if (hit && picked.length < 3) picked.push(hit);
        });
        ordered.forEach(function (p) {
            if (picked.length < 3 && picked.indexOf(p) < 0) picked.push(p);
        });
        return picked.map(function (p) { return p.text.split("{name}").join(AI_PARTNER); });
    }

    function renderSuggestions() {
        const items = buildSuggestions();
        lastShown = items;
        suggestionList.innerHTML = "";
        items.forEach(function (text) {
            const card = document.createElement("div");
            card.className = "suggestion-card";
            const p = document.createElement("p");
            p.textContent = text;
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "copy-suggestion";
            btn.textContent = "▢ コピー";
            card.appendChild(p);
            card.appendChild(btn);
            suggestionList.appendChild(card);
        });
    }

    function renderSummary() {
        const conv = readConversation();
        const lines = ["イベントで知り合った（" + AI_PARTNER + "さん）"];
        lines.push(AI_TAGS.length
                ? "共通の興味：" + AI_TAGS.join("・")
                : "共通の興味はまだ見つかっていない");
        lines.push("メッセージ：あなた " + conv.myCount + "件 ／ " + AI_PARTNER + "さん " + conv.otherCount + "件");
        if (conv.last) {
            lines.push("相手の最後のメッセージ：「" + (conv.last.length > 20 ? conv.last.slice(0, 20) + "…" : conv.last) + "」");
        }
        summaryList.innerHTML = "";
        lines.forEach(function (text) {
            const li = document.createElement("li");
            li.textContent = text;
            summaryList.appendChild(li);
        });
    }

    /** 内蔵の提案（Gemini に繋がらないときの予備） */
    function refreshLocal() {
        renderSummary();
        renderSuggestions();
    }

    // ---------- Gemini API（サーバーの /chat/ai 経由。API キーは画面に出さない） ----------
    const AI_URL = "<%= ctx %>/chat/ai";
    const aiNote = document.querySelector(".gemini-description");
    const aiNoteDefault = aiNote ? aiNote.textContent : "";
    let aiBusy = false;

    function setNote(text) {
        if (aiNote) aiNote.textContent = text;
    }

    function fillList(listEl, items, asCards) {
        listEl.innerHTML = "";
        items.forEach(function (text) {
            if (!asCards) {
                const li = document.createElement("li");
                li.textContent = text;
                listEl.appendChild(li);
                return;
            }
            const card = document.createElement("div");
            card.className = "suggestion-card";
            const p = document.createElement("p");
            p.textContent = text;
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "copy-suggestion";
            btn.textContent = "▢ コピー";
            card.appendChild(p);
            card.appendChild(btn);
            listEl.appendChild(card);
        });
    }

    /** Gemini の返事から {"summary":[…],"suggestions":[…]} を取り出す */
    function parseGemini(data) {
        const parts = (((data.candidates || [])[0] || {}).content || {}).parts || [];
        const text = parts.filter(function (p) { return !p.thought && p.text; })
                          .map(function (p) { return p.text; }).join("");
        const start = text.indexOf("{");
        const end = text.lastIndexOf("}");
        if (start < 0 || end <= start) throw new Error("JSON が見つかりません");
        const obj = JSON.parse(text.slice(start, end + 1));
        const clean = function (arr) {
            return (Array.isArray(arr) ? arr : [])
                .map(function (x) { return String(x).trim(); })
                .filter(function (x) { return x.length > 0; })
                .slice(0, 3);
        };
        const result = { summary: clean(obj.summary), suggestions: clean(obj.suggestions) };
        if (result.suggestions.length === 0) throw new Error("提案が空です");
        return result;
    }

    function refreshAi() {
        if (aiBusy) return;
        aiBusy = true;

        const label = "↻ 別の提案を生成";
        regenButton.disabled = true;
        regenButton.textContent = "… Gemini が考え中";
        suggestionList.style.opacity = "0.4";

        const body = new URLSearchParams();
        body.set("partnerId", "<%= partnerId %>");
        body.set("tone", toneSelect.value);

        fetch(AI_URL, { method: "POST", body: body, credentials: "same-origin" })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.error) throw new Error(data.message || data.error);
                const r = parseGemini(data);
                if (r.summary.length) fillList(summaryList, r.summary, false);
                fillList(suggestionList, r.suggestions, true);
                lastShown = r.suggestions;
                setNote(aiNoteDefault);
            })
            .catch(function (err) {
                // キーが無い・通信できない など → 内蔵の提案を出す
                console.log("[Gemini] 使えないので内蔵の提案にします：", err.message);
                refreshLocal();
                setNote("※ Gemini に接続できないため、簡易的な提案を表示しています。");
            })
            .then(function () {
                suggestionList.style.opacity = "";
                regenButton.textContent = label;
                regenButton.disabled = false;
                aiBusy = false;
            });
    }

    regenButton.addEventListener("click", refreshAi);
    toneSelect.addEventListener("change", refreshAi);

    // 「コピー」：入力欄に入れる（自動では送らない）
    suggestionList.addEventListener("click", function (e) {
        const btn = e.target.closest(".copy-suggestion");
        if (!btn) return;
        input.value = btn.parentElement.querySelector("p").textContent.trim();
        closeAi();
        input.focus();
    });

    refreshLocal();      // 最初は内蔵の提案（Gemini は AI パネルを開いたときに呼ぶ）

    // ===== 交流申請のお知らせ：5秒ごとに状態を確認して、チャットの上に出す =====
    const reqBanner = document.getElementById("requestBanner");
    const reqText = document.getElementById("requestBannerText");
    // 見送りのお知らせは、確認したら（帯を押したら）もう出さない
    function seenDecline(id) {
        try { return localStorage.getItem("c2r-decline-seen-" + id) === "1"; } catch (e) { return false; }
    }
    reqBanner.addEventListener("click", function () {
        const id = reqBanner.dataset.declineId;
        if (id && reqBanner.classList.contains("is-soft")) {
            try { localStorage.setItem("c2r-decline-seen-" + id, "1"); } catch (e) {}
        }
    });

    function checkRequest() {
        if (document.hidden) return;
        fetch("<%= ctx %>/interaction/request?partnerId=<%= partnerId %>&format=json",
              { cache: "no-store", credentials: "same-origin" })
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (s) {
                if (!s) return;
                let text = "";
                if (s.approved) {
                    text = "相互承認が成立しました！合流しましょう";
                    reqBanner.classList.add("is-match");
                } else if (s.incoming) {
                    text = AI_PARTNER + "さんから交流申請が届きました";
                    reqBanner.classList.remove("is-match");
                } else if (s.waiting) {
                    text = "交流申請を送りました。返事を待っています";
                    reqBanner.classList.remove("is-match");
                } else if (s.declined && !seenDecline(s.exchangeRequestId)) {
                    // 見送られたときは、責めない言い方で1回だけお知らせ（押すとお知らせ画面へ）
                    text = "今回は見送りになりました。チャットはこのまま続けられます 🌱";
                    reqBanner.classList.remove("is-match");
                    reqBanner.classList.add("is-soft");
                    reqBanner.dataset.declineId = s.exchangeRequestId;
                }
                if (!s.declined) reqBanner.classList.remove("is-soft");
                reqText.textContent = text;
                reqBanner.hidden = (text === "");
            })
            .catch(function () {});
    }
    checkRequest();
    setInterval(checkRequest, 5000);

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
        refreshAi();                       // 開くたびに最新の会話で作り直す
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
