<%@ page pageEncoding="UTF-8" %>
<%--
  交流申請のお知らせ（参加者一覧・プロフィールなど、どの画面でも）
    ・5秒ごとに /interaction/notify に聞く
    ・申請が届いた／相互承認が成立した／申請が見送られた ときに、画面の上にカードを出す（1件につき1回だけ）
    ・id="requestBadge" の要素に「返事を待っている申請の数」を出す（参加者一覧の「交流申請」ボタン）
    ・id="requestButton" のボタンを押すと、お知らせの一覧を下に開く
    ・参加者一覧のカード（data-id）に「💌 申請が届いています」を付ける
  使い方：footer.jsp の include のすぐ上に
          <%@ include file="/common/request-notify.jsp" %>
  ※ ほかの JSP と名前がぶつからないよう、クラス名・変数は rn で始めている
--%>
<style>
    /* 「交流申請」ボタンの赤い丸 */
    .rn-badge {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 20px;
        height: 20px;
        padding: 0 6px;
        box-sizing: border-box;
        color: #ffffff;
        background: #ff3b5c;
        border: 2px solid #ffffff;
        border-radius: 999px;
        box-shadow: 0 2px 6px rgba(255, 59, 92, 0.35);
        font-size: 11px;
        font-weight: 800;
        line-height: 1;
    }
    .rn-badge[hidden] { display: none; }

    /* 上から出てくるお知らせカード */
    .rn-toast {
        position: fixed;
        top: 12px;
        left: 50%;
        z-index: 2090;
        display: flex;
        align-items: center;
        gap: 12px;
        width: min(420px, calc(100vw - 24px));
        padding: 12px 14px;
        box-sizing: border-box;
        color: #233b59;
        background: rgba(255, 255, 255, 0.98);
        border: 1px solid #f6c9da;
        border-radius: 18px;
        box-shadow: 0 14px 36px rgba(40, 66, 105, 0.22);
        font-family: "Noto Sans JP", "Yu Gothic", "Meiryo", sans-serif;
        text-decoration: none;
        cursor: pointer;
        transform: translate(-50%, -150%);
        transition: transform 0.35s cubic-bezier(0.2, 0.9, 0.3, 1.2);
    }
    .rn-toast.is-show { transform: translate(-50%, 0); }
    .rn-toast.is-match { border-color: #bfe6d3; }
    .rn-toast.is-soft  { border-color: #dde3f0; }

    .rn-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        width: 44px;
        height: 44px;
        background: #fff0f5;
        border-radius: 50%;
        font-size: 22px;
    }
    .rn-toast.is-match .rn-icon { background: #eaf7f0; }
    .rn-toast.is-soft  .rn-icon { background: #f1f4fa; }

    .rn-body {
        display: flex;
        flex: 1;
        flex-direction: column;
        gap: 2px;
        min-width: 0;
    }
    .rn-title {
        color: #173b63;
        font-size: 14px;
        font-weight: 800;
    }
    .rn-text {
        overflow: hidden;
        color: #4a5b74;
        font-size: 13px;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    .rn-close {
        flex-shrink: 0;
        padding: 4px 6px;
        color: #9aa6b8;
        background: none;
        border: none;
        font-size: 18px;
        line-height: 1;
        cursor: pointer;
    }

    /* 「交流申請」ボタンの下に開くお知らせ一覧 */
    .rn-panel {
        position: absolute;
        top: calc(100% + 8px);
        right: 0;
        z-index: 800;
        width: min(340px, calc(100vw - 32px));
        padding: 8px;
        box-sizing: border-box;
        background: #ffffff;
        border: 1px solid #e6ecf5;
        border-radius: 16px;
        box-shadow: 0 14px 36px rgba(40, 66, 105, 0.18);
    }
    .rn-panel[hidden] { display: none; }
    .rn-panel-title {
        margin: 4px 8px 6px;
        color: #7184a0;
        font-size: 12px;
        font-weight: 800;
    }
    .rn-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px;
        color: #233b59;
        border-radius: 12px;
        text-decoration: none;
    }
    .rn-item:hover { background: #f6f8fc; }
    .rn-item-icon { font-size: 20px; flex-shrink: 0; }
    .rn-item-text { flex: 1; min-width: 0; font-size: 13px; line-height: 1.5; }
    .rn-item-text strong { color: #173b63; }
    .rn-item-go { flex-shrink: 0; color: #f45b91; font-size: 12px; font-weight: 800; }
    .rn-item.is-incoming { background: #fff6f9; }
    .rn-empty {
        margin: 6px 8px 10px;
        color: #8a97aa;
        font-size: 13px;
    }

    /* 参加者一覧のカードの名前の横に付ける「申請が届いています」 */
    .participant-name-row:has(.rn-pill) { display: flex; flex-wrap: wrap; align-items: center; }
    .rn-pill {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        margin: 2px 0 2px 8px;
        padding: 3px 10px;
        vertical-align: middle;
        color: #e23a7a;
        background: #fff0f5;
        border: 1px solid #f9c3d7;
        border-radius: 999px;
        font-size: 12px;
        font-weight: 800;
        text-decoration: none;
        white-space: nowrap;
    }

    @media (prefers-reduced-motion: reduce) {
        .rn-toast { transition: none; }
    }
</style>

<a class="rn-toast" id="rnToast" href="#" role="status" aria-live="polite">
    <span class="rn-icon" id="rnIcon">💌</span>
    <span class="rn-body">
        <span class="rn-title" id="rnTitle"></span>
        <span class="rn-text" id="rnText"></span>
    </span>
    <button type="button" class="rn-close" id="rnClose" aria-label="閉じる">×</button>
</a>

<script>
(function () {
    var CTX = "<%= request.getContextPath() %>";
    var API = CTX + "/interaction/notify";

    var toast = document.getElementById("rnToast");
    var hideTimer = null;
    var latest = [];          // いちばん新しいお知らせ一覧
    var queue = [];           // まだ出していないカード

    // ----- もう見たか（このブラウザで覚える） -----
    function seenKey(n) { return "c2r-rn-" + n.type + "-" + n.id; }
    function isSeen(n) {
        try { return localStorage.getItem(seenKey(n)) === "1"; } catch (e) { return false; }
    }
    function setSeen(n) {
        try {
            localStorage.setItem(seenKey(n), "1");
            // チャット画面の「見送り」の帯も、もう出さない
            if (n.type === "declined") localStorage.setItem("c2r-decline-seen-" + n.id, "1");
        } catch (e) {}
    }

    function linkOf(n) {
        return n.type === "approved"
            ? CTX + "/participant/MutualConsent.action?exchangeRequestId=" + n.id
            : CTX + "/interaction/request?partnerId=" + n.partnerId;
    }

    // ----- お知らせの文 -----
    function words(n) {
        var name = n.partnerName + "さん";
        if (n.type === "incoming") {
            return { icon: "💌", cls: "", title: "交流申請が届きました",
                     text: name + "が会場で直接話したいと思っています", short: "<strong>" + esc(name) + "</strong>から交流申請が届いています", go: "返事する" };
        }
        if (n.type === "approved") {
            return { icon: "🎉", cls: "is-match", title: "相互承認が成立しました！",
                     text: name + "と合流しましょう", short: "<strong>" + esc(name) + "</strong>と相互承認が成立しました", go: "合流へ" };
        }
        return { icon: "🌱", cls: "is-soft", title: "今回は見送りになりました",
                 text: "チャットはこのまま続けられます", short: esc(name) + "への申請は、今回は見送りになりました", go: "見る" };
    }
    function esc(s) {
        return String(s).replace(/[&<>"']/g, function (c) {
            return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
        });
    }

    // ----- 上から出てくるカード -----
    function showNext() {
        if (toast.classList.contains("is-show") || queue.length === 0) return;
        var n = queue.shift();
        var w = words(n);
        setSeen(n);
        toast.className = "rn-toast " + w.cls;
        toast.href = linkOf(n);
        document.getElementById("rnIcon").textContent = w.icon;
        document.getElementById("rnTitle").textContent = w.title;
        document.getElementById("rnText").textContent = w.text;
        requestAnimationFrame(function () { toast.classList.add("is-show"); });
        clearTimeout(hideTimer);
        hideTimer = setTimeout(hide, n.type === "incoming" ? 9000 : 6000);
    }
    function hide() {
        toast.classList.remove("is-show");
        setTimeout(showNext, 450);          // 次のお知らせがあれば続けて出す
    }
    document.getElementById("rnClose").addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        clearTimeout(hideTimer);
        hide();
    });

    // ----- 「交流申請」ボタン・赤い丸・一覧 -----
    var button = document.getElementById("requestButton");
    var badge = document.getElementById("requestBadge");
    var panel = null;

    if (button) {
        var wrap = button.parentNode;
        if (getComputedStyle(wrap).position === "static") wrap.style.position = "relative";
        panel = document.createElement("div");
        panel.className = "rn-panel";
        panel.id = "rnPanel";
        panel.hidden = true;
        wrap.appendChild(panel);

        button.addEventListener("click", function (e) {
            e.stopPropagation();
            panel.hidden = !panel.hidden;
            if (!panel.hidden) renderPanel();
        });
        document.addEventListener("click", function (e) {
            if (!panel.hidden && !panel.contains(e.target)) panel.hidden = true;
        });
    }

    function renderPanel() {
        if (!panel) return;
        var html = '<p class="rn-panel-title">交流申請のお知らせ</p>';
        if (latest.length === 0) {
            html += '<p class="rn-empty">まだお知らせはありません。<br>チャットで「申請」を押すと、相手に届きます。</p>';
        }
        latest.forEach(function (n) {
            var w = words(n);
            html += '<a class="rn-item' + (n.type === "incoming" ? " is-incoming" : "") + '" href="' + linkOf(n) + '">'
                  + '<span class="rn-item-icon">' + w.icon + '</span>'
                  + '<span class="rn-item-text">' + w.short + '</span>'
                  + '<span class="rn-item-go">' + w.go + ' ›</span></a>';
        });
        panel.innerHTML = html;
    }

    // 参加者一覧のカードに「💌 申請が届いています」
    function markCards() {
        var incoming = {};
        latest.forEach(function (n) { if (n.type === "incoming") incoming[n.partnerId] = n; });
        document.querySelectorAll(".participant-card[data-id]").forEach(function (card) {
            var id = card.getAttribute("data-id");
            var pill = card.querySelector(".rn-pill");
            if (incoming[id] && !pill) {
                // 名前の横に付ける（カード全体がプロフィールへのリンクなので <span>）
                pill = document.createElement("span");
                pill.className = "rn-pill";
                pill.textContent = "💌 申請が届いています";
                var row = card.querySelector(".participant-name-row") || card;
                row.appendChild(pill);
            } else if (!incoming[id] && pill) {
                pill.remove();
            }
        });
    }

    // ----- サーバーに聞く -----
    function check() {
        fetch(API, { cache: "no-store", credentials: "same-origin" })
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (data) {
                if (!data || !data.notices) return;
                latest = data.notices;

                // 赤い丸 = 返事を待っている（自分に届いている）申請の数
                var count = latest.filter(function (n) { return n.type === "incoming"; }).length;
                if (badge) {
                    badge.textContent = count > 9 ? "9+" : String(count);
                    badge.hidden = count === 0;
                }

                // まだ出していないカードを古い順に出す
                latest.slice().reverse().forEach(function (n) {
                    if (!isSeen(n) && queue.indexOf(n) < 0 &&
                        !queue.some(function (q) { return q.id === n.id && q.type === n.type; })) {
                        queue.push(n);
                    }
                });
                showNext();

                if (panel && !panel.hidden) renderPanel();
                markCards();
            })
            .catch(function () { /* 通信できないときは次の回にまた聞く */ });
    }

    check();
    setInterval(function () { if (!document.hidden) check(); }, 5000);
    document.addEventListener("visibilitychange", function () { if (!document.hidden) check(); });
})();
</script>
