<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.net.URLEncoder, java.nio.charset.StandardCharsets" %>
<%@ page import="Bean.ParticipantListBean, Bean.InterestBean" %>
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
 
    private static String url(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
 
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
%>
<%
    String ctx = request.getContextPath();
 
    // ===== サーブレット（ParticipantListAction）から受け取るデータ =====
    ParticipantListBean list = (ParticipantListBean) request.getAttribute("list");
    String chatBase = (String) request.getAttribute("chatBase");
 
    // Action を通さずに開いたときは Action から開き直す
    if (list == null) {
        response.sendRedirect(ctx + "/participant/ParticipantList.action");
        return;
    }
 
    // 「すべて表示」リンク（今の検索条件のまま）
    String showAllUrl = ctx + "/participant/ParticipantList.action?all=1"
            + "&keyword=" + url(list.getKeyword())
            + "&ageGroup=" + url(list.getAgeGroup())
            + (list.getInterestId() > 0 ? "&interestId=" + list.getInterestId() : "");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
 
    <title>参加者一覧 | Chat2Real</title>
 
    <link rel="stylesheet"
          href="<%= ctx %>/css/participant-list.css">
</head>
 
<body class="participant-list-page">
 
<%-- 参加者用ヘッダー --%>
<%@ include file="/common/header-participants.jsp" %>
 
<main class="participant-page">
 
    <!-- Page Header -->
       <section class="page-heading">
        <div>
            <span class="page-eyebrow">COMMUNITY</span>
            <h1>参加者一覧</h1>
            <p class="page-description">
                気になる参加者を見つけて、チャットで交流を始めましょう。
            </p>
        </div>
 
        <div class="heading-side">
            <div class="participant-count">
                <span class="count-number" id="liveCount"><%= list.getWantCount() %></span>
                <span class="count-label">交流したい参加者</span>
            </div>
 
            <%-- ChatAction（/chat）：最後にチャットした相手の画面を開く。まだだれともしていなければ「相手を選んでください」 --%>
            <% if (list.isCanChat()) { %>
            <a href="<%= ctx %>/chat" class="open-chat-button">
                <span aria-hidden="true">💬</span> チャット画面へ
                 <span class="cn-badge" id="chatUnreadBadge" hidden>0</span>
            </a>
        <% } else { %>
            <button type="button" class="open-chat-button" disabled>
                <span aria-hidden="true">💬</span> チャット画面へ
            </button>
        <% } %>
 
            <%-- 交流申請のお知らせ（押すと一覧が開く）。赤い丸は返事を待っている申請の数 --%>
            <div class="request-wrap">
                <button type="button" class="open-chat-button request-button" id="requestButton"
                        aria-haspopup="true">
                    <span aria-hidden="true">💌</span> 交流申請
                    <span class="rn-badge" id="requestBadge" hidden>0</span>
                </button>
            </div>
        </div>
    </section>
 
<% if (!list.isCanChat()) { %>
    <!-- 自分が「今は交流したくない」のとき -->
    <div class="status-warning" role="status">
        <span class="notice-icon">!</span>
        <p>
            今は「今は交流したくない」を選んでいるため、チャットできません。
            <a href="<%= ctx %>/participant/Top.action">イベントルームで「交流したい」に切り替える →</a>
        </p>
    </div>
<% } %>
 
 
    <!-- Search Section（検索・絞り込みは1つのフォーム） -->
    <section class="search-panel">
 
        <form action="<%= ctx %>/participant/ParticipantList.action" method="get" class="search-form" id="searchForm">
 
            <label for="participantName" class="search-label">
                参加者を検索
            </label>
 
            <div class="search-input-group">
                <input
                    type="text"
                    id="participantName"
                    name="keyword"
                    maxlength="20"
                    value="<%= h(list.getKeyword()) %>"
                    placeholder="ニックネームを入力してください">
 
                <button type="submit" class="search-button">
                    検索
                </button>
            </div>
 
 
            <!-- Filter Section -->
            <div class="filter-section">
 
                <div class="filter-group">
                    <label for="ageFilter" class="filter-label">年代</label>
 
                    <select id="ageFilter" name="ageGroup" class="filter-select">
                        <option value="">すべて</option>
                    <% for (String age : list.getAgeGroupOptions()) { %>
                        <option value="<%= h(age) %>" <%= age.equals(list.getAgeGroup()) ? "selected" : "" %>><%= h(age) %></option>
                    <% } %>
                    </select>
                </div>
 
 
                <div class="filter-group hobby-filter">
                    <label for="hobbyFilter" class="filter-label">
                        趣味
                    </label>
 
                    <select id="hobbyFilter" name="interestId" class="filter-select">
                        <option value="">すべて</option>
                    <% for (InterestBean opt : list.getInterestOptions()) { %>
                        <option value="<%= opt.getInterestId() %>" <%= opt.getInterestId() == list.getInterestId() ? "selected" : "" %>><%= h(opt.getInterestName()) %></option>
                    <% } %>
                    </select>
                </div>
 
            <% if (list.isFiltered()) { %>
                <a href="<%= ctx %>/participant/ParticipantList.action" class="clear-link">条件をクリア</a>
            <% } %>
 
            </div>
 
        </form>
 
    </section>
 
 
    <!-- 新しい参加者が入ってきたときのお知らせ（自動更新） -->
    <button type="button" class="live-toast" id="liveToast" hidden></button>
 
    <!-- Participant List -->
    <section class="participant-section">
 
        <div class="list-heading" id="liveHeading">
            <h2><%= list.isFiltered() ? "検索結果" : "交流できる参加者" %></h2>
            <span class="list-description">
            <% if (list.isFiltered()) { %>
                <%= list.getHitCount() %>名が見つかりました
            <% } else { %>
                気になる相手を選んでみましょう
            <% } %>
            </span>
        </div>
 
 
        <div class="participant-list" id="liveList">
 
        <% for (ParticipantListBean.Participant p : list.getParticipants()) {
               String profileUrl = ctx + "/participant/AccountProfile.action?id=" + p.getEventParticipantId(); %>
            <article class="participant-card" data-id="<%= p.getEventParticipantId() %>">
 
                <!-- アイコン・名前・興味・一言（押すとプロフィール） -->
                <a href="<%= profileUrl %>" class="participant-link" aria-label="<%= h(p.getNickname()) %>さんのプロフィールを見る">
 
                    <div class="participant-avatar <%= p.getAvatarClass() %>">
                    <% if (!isBlank(p.getProfileImageUrl())) { %>
                        <img src="<%= ctx %><%= h(p.getProfileImageUrl()) %>" alt="">
                    <% } else { %>
                        <span><%= h(p.getInitial()) %></span>
                    <% } %>
                    </div>
 
                    <div class="participant-information">
 
                        <div class="participant-name-row">
                            <h3><%= h(p.getNickname()) %> <span class="reading"><%= h(p.getAgeGroup()) %></span></h3>
                        </div>
 
                    <% if (!p.getInterests().isEmpty()) { %>
                        <div class="hobby-tags">
                        <% for (String tag : p.getInterests()) {
                               boolean common = list.getMyInterests().contains(tag); %>
                            <span class="hobby-tag<%= common ? " is-common" : "" %>"<%= common ? " title=\"あなたと共通の興味\"" : "" %>><%= h(tag) %></span>
                        <% } %>
                        </div>
                    <% } %>
 
                    <% if (!isBlank(p.getSelfIntroduction())) { %>
                        <p class="participant-introduction">
                            <%= h(p.getSelfIntroduction()) %>
                        </p>
                    <% } %>
 
                    </div>
                </a>
 
                <div class="participant-action">
                <% if (list.isCanChat()) { %>
                    <a href="<%= h(chatBase) %>?partnerId=<%= p.getEventParticipantId() %>" class="chat-button">
                        チャットする
                         <span class="cn-badge" data-unread-partner="<%= p.getEventParticipantId() %>" hidden>0</span>
                    </a>
                <% } else { %>
                    <button type="button" class="chat-button" disabled>チャットする</button>
                <% } %>
                </div>
 
            </article>
        <% } %>
 
        </div>
 
 
        <!-- Empty State -->
        <div class="empty-message" id="liveEmpty" <%= list.getParticipants().isEmpty() ? "" : "hidden" %>>
            <div class="empty-icon">⌕</div>
        <% if (list.isFiltered()) { %>
            <h3>参加者が見つかりません</h3>
            <p>検索条件を変更して、もう一度お試しください。</p>
        <% } else { %>
            <h3>まだ交流したい参加者がいません</h3>
            <p>参加者が増えると、ここに表示されます。</p>
        <% } %>
        </div>
 
 
        <div id="liveFooter">
    <% if (list.getOtherCount() > 0) { %>
        <div class="list-footer">
            <p>ほかにも交流できる参加者が <%= list.getOtherCount() %> 名います</p>
            <a href="<%= h(showAllUrl) %>" class="show-all-link">すべて表示する</a>
        </div>
    <% } %>
        </div>
 
    </section>
 
 
    <!-- Notice -->
    <div class="participant-notice">
        <span class="notice-icon">i</span>
        <p>
            交流したい参加者のみ表示しています。
            カードを押すとプロフィール、「チャットする」から1対1のチャットを開始できます。
        </p>
    </div>
 
    <!-- Event Materials / My Profile -->
    <section class="event-materials">
        <a href="<%= ctx %>/participant/MaterialsView.action"
           class="event-materials-link">
            イベント資料
            <span class="link-arrow">→</span>
        </a>
        <a href="<%= ctx %>/participant/AccountProfile.action"
           class="event-materials-link">
            自分のプロフィール
            <span class="link-arrow">→</span>
        </a>
    </section>
 
</main>
 
<style>
    /* 「交流申請」ボタン（チャット画面へ の下） */
    .request-wrap {
        position: relative;
        display: flex;
    }
    .request-wrap .request-button {
        flex: 1;
        border-color: #c9d6ea;
        color: #173b63 !important;
    }
    .request-wrap .request-button:hover {
        background: #f4f7fc;
    }
    @media (max-width: 700px) {
        /* スマホ：人数・チャット画面へ の下に横いっぱいで */
        .heading-side { flex-wrap: wrap; }
        .request-wrap { flex-basis: 100%; }
        .request-wrap .request-button { border-radius: 18px; }
    }
 
    /* 新しい参加者のお知らせ */
    .live-toast {
        position: fixed;
        top: 14px;
        left: 50%;
        z-index: 900;
        transform: translate(-50%, -12px);
        opacity: 0;
 
        padding: 10px 18px;
        color: #ffffff;
        background: #173b63;
        border: none;
        border-radius: 999px;
        box-shadow: 0 8px 22px rgba(23, 59, 99, 0.28);
 
        font-family: inherit;
        font-size: 14px;
        font-weight: 700;
        white-space: nowrap;
        cursor: pointer;
        pointer-events: none;
        transition: opacity 0.25s, transform 0.25s;
    }
    .live-toast.is-show {
        opacity: 1;
        pointer-events: auto;
        transform: translate(-50%, 0);
    }
    /* 新しく入ってきた人のカードを少し光らせる */
    .participant-card.is-new {
        animation: live-new 2.4s ease-out;
    }
    @keyframes live-new {
        0%   { box-shadow: 0 0 0 3px rgba(244, 91, 145, 0.55); }
        100% { box-shadow: 0 0 0 3px rgba(244, 91, 145, 0); }
    }
</style>
 
<script>
    // ===== 参加者一覧の自動更新 =====
    // 15秒ごとに同じ条件で一覧を取り直し、変わっていたら一覧の部分だけ入れ替える（画面の再読み込みはしない）
    (function () {
        var INTERVAL = 15000;
        var toast = document.getElementById("liveToast");
        var toastTimer = null;
 
        function ids(root) {
            return Array.prototype.map.call(root.querySelectorAll(".participant-card[data-id]"),
                function (el) { return el.getAttribute("data-id"); });
        }
 
        function showToast(text) {
            toast.textContent = text;
            toast.hidden = false;
            requestAnimationFrame(function () { toast.classList.add("is-show"); });
            clearTimeout(toastTimer);
            toastTimer = setTimeout(function () { toast.classList.remove("is-show"); }, 5000);
        }
        toast.addEventListener("click", function () {
            toast.classList.remove("is-show");
            document.getElementById("liveHeading").scrollIntoView({ behavior: "smooth", block: "start" });
        });
 
        function replace(id, doc) {
            var now = document.getElementById(id);
            var next = doc.getElementById(id);
            if (now && next) now.replaceWith(next);
        }
 
        function refresh() {
            if (document.hidden) return;
            // 検索欄に入力中のときは更新しない
            if (document.activeElement && document.activeElement.id === "participantName") return;
 
            fetch(location.href, { cache: "no-store", credentials: "same-origin" })
                .then(function (r) { return (r.ok && !r.redirected) ? r.text() : null; })
                .then(function (html) {
                    if (!html) return;
                    var doc = new DOMParser().parseFromString(html, "text/html");
                    var nextList = doc.getElementById("liveList");
                    if (!nextList) return;
 
                    var before = ids(document);
                    var after = ids(doc);
                    var countNow = document.getElementById("liveCount");
                    var countNext = doc.getElementById("liveCount");
                    var sameCount = !countNow || !countNext || countNow.textContent === countNext.textContent;
                    if (before.join(",") === after.join(",") && sameCount) return;   // 変化なし
 
                    // 未読バッジの今の表示を引き継ぐ（次の通知チェックまで消えないように）
                    var badges = {};
                    document.querySelectorAll("#liveList [data-unread-partner]").forEach(function (b) {
                        badges[b.getAttribute("data-unread-partner")] = { hidden: b.hidden, text: b.textContent };
                    });
 
                    replace("liveList", doc);
                    replace("liveHeading", doc);
                    replace("liveEmpty", doc);
                    replace("liveFooter", doc);
                    if (countNow && countNext) countNow.textContent = countNext.textContent;
 
                    document.querySelectorAll("#liveList [data-unread-partner]").forEach(function (b) {
                        var old = badges[b.getAttribute("data-unread-partner")];
                        if (old) { b.hidden = old.hidden; b.textContent = old.text; }
                    });
 
                    // 新しく入ってきた人
                    var added = after.filter(function (id) { return before.indexOf(id) < 0; });
                    added.forEach(function (id) {
                        var card = document.querySelector('#liveList .participant-card[data-id="' + id + '"]');
                        if (card) card.classList.add("is-new");
                    });
                    if (added.length > 0) {
                        showToast("👋 新しい参加者が" + added.length + "人入りました");
                    }
                })
                .catch(function () { /* 通信できないときは次の回に */ });
        }
 
        setInterval(refresh, INTERVAL);
        // ほかのアプリから戻ってきたときもすぐ確認
        document.addEventListener("visibilitychange", function () {
            if (!document.hidden) refresh();
        });
    })();
 
    // 年代・趣味を選んだらすぐに絞り込む
    document.querySelectorAll("#searchForm .filter-select").forEach(function (select) {
        select.addEventListener("change", function () {
            document.getElementById("searchForm").submit();
        });
    });
</script>
 
<%-- 交流申請のお知らせ（申請が届いた・相互承認が成立した など） --%>
<%@ include file="/common/request-notify.jsp" %>
 
<%-- 新着メッセージ通知（未読バッジ）※ <!DOCTYPE> より前に置くと画面が崩れるので、ここに置く --%>
<%@ include file="/common/chat-notify.jsp" %>
 
<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
 
 