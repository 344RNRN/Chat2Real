<%@ page pageEncoding="UTF-8" %>
<%--
  新着メッセージ通知（Instagram・Messenger 風）
    ・5秒ごとに /participant/ChatUnread.action に聞く
    ・新しいメッセージが来たら、画面の上に「○○さん：メッセージ」のカードを出す（押すとそのチャットへ）
    ・id="chatUnreadBadge" の要素に未読の合計を出す（参加者一覧の「チャット画面へ」）
    ・data-unread-partner="相手の event_participant_id" の要素に、その人からの未読数を出す
  使い方：参加者の画面の footer.jsp の include のすぐ上に
          <%@ include file="/common/chat-notify.jsp" %>
  チャット画面では、いま話している相手の通知を出さないように、include の前に
          <script>window.CHAT_NOTIFY_IGNORE = 相手のID;</script>
  ※ ほかの JSP と名前がぶつからないよう、クラス名・変数は cn で始めている
--%>
<style>
    /* 未読バッジ（赤い丸） */
    .cn-badge {
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

    .cn-badge[hidden] {
        display: none;
    }

    .cn-badge.cn-pop {
        animation: cn-pop 0.35s ease;
    }

    @keyframes cn-pop {
        0%   { transform: scale(0.6); }
        60%  { transform: scale(1.2); }
        100% { transform: scale(1); }
    }

    /* 上から出てくる通知カード */
    .cn-toast {
        position: fixed;
        top: 12px;
        left: 50%;
        z-index: 2100;

        display: flex;
        align-items: center;
        gap: 12px;

        width: min(420px, calc(100vw - 24px));
        padding: 12px 14px;
        box-sizing: border-box;

        color: #233b59;
        background: rgba(255, 255, 255, 0.98);
        border: 1px solid #e6ecf5;
        border-radius: 18px;
        box-shadow: 0 14px 36px rgba(40, 66, 105, 0.22);

        font-family: "Noto Sans JP", "Yu Gothic", "Meiryo", sans-serif;
        text-decoration: none;
        cursor: pointer;

        transform: translate(-50%, -150%);
        transition: transform 0.35s cubic-bezier(0.2, 0.9, 0.3, 1.2);
    }

    .cn-toast.is-show {
        transform: translate(-50%, 0);
    }

    .cn-avatar {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        width: 44px;
        height: 44px;

        color: #ffffff;
        background: linear-gradient(135deg, #ff7eaa 0%, #a074f0 100%);
        border-radius: 50%;

        font-size: 18px;
        font-weight: 800;
    }

    .cn-body {
        display: flex;
        flex: 1;
        flex-direction: column;
        gap: 2px;
        min-width: 0;
    }

    .cn-top {
        display: flex;
        align-items: baseline;
        gap: 8px;
    }

    .cn-name {
        overflow: hidden;
        color: #173b63;
        font-size: 14px;
        font-weight: 800;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .cn-time {
        flex-shrink: 0;
        color: #9aa6b8;
        font-size: 11px;
    }

    .cn-text {
        overflow: hidden;
        color: #4a5b74;
        font-size: 13px;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .cn-close {
        flex-shrink: 0;
        padding: 4px 6px;
        color: #9aa6b8;
        background: none;
        border: none;
        font-size: 18px;
        line-height: 1;
        cursor: pointer;
    }

    @media (prefers-reduced-motion: reduce) {
        .cn-toast { transition: none; }
        .cn-badge.cn-pop { animation: none; }
    }
</style>

<a class="cn-toast" id="cnToast" href="#" role="status" aria-live="polite">
    <span class="cn-avatar" id="cnAvatar">?</span>
    <span class="cn-body">
        <span class="cn-top">
            <span class="cn-name" id="cnName"></span>
            <span class="cn-time">いま</span>
        </span>
        <span class="cn-text" id="cnText"></span>
    </span>
    <button type="button" class="cn-close" id="cnClose" aria-label="閉じる">×</button>
</a>

<script>
(function () {
    var CTX = "<%= request.getContextPath() %>";
    var API = CTX + "/participant/ChatUnread.action";
    var IGNORE = String(window.CHAT_NOTIFY_IGNORE || "");
    var KEY = "cnLastToastId";

    var toast = document.getElementById("cnToast");
    var baseTitle = document.title;
    var hideTimer = null;
    var lastTotal = -1;

    function getSeen() {
        try { return Number(sessionStorage.getItem(KEY) || 0); } catch (e) { return 0; }
    }
    function setSeen(id) {
        try { sessionStorage.setItem(KEY, String(id)); } catch (e) { /* 保存できなくても動く */ }
    }
    var seen = getSeen();

    document.getElementById("cnClose").addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        toast.classList.remove("is-show");
    });

    function showToast(latest) {
        document.getElementById("cnName").textContent = latest.name + " さん";
        document.getElementById("cnText").textContent = latest.text;
        document.getElementById("cnAvatar").textContent = (latest.name || "?").charAt(0);
        toast.href = CTX + "/chat?partnerId=" + latest.partnerId;

        toast.classList.add("is-show");
        clearTimeout(hideTimer);
        hideTimer = setTimeout(function () { toast.classList.remove("is-show"); }, 6000);
    }

    function setBadge(el, count) {
        if (!el) return;
        var before = el.textContent;
        el.textContent = count > 99 ? "99+" : String(count);
        el.hidden = count <= 0;
        if (count > 0 && before !== el.textContent) {
            el.classList.remove("cn-pop");
            void el.offsetWidth;            // アニメーションをもう一度動かす
            el.classList.add("cn-pop");
        }
    }

    function check() {
        fetch(API, { cache: "no-store", credentials: "same-origin" })
            .then(function (res) { return res.ok ? res.json() : null; })
            .then(function (data) {
                if (!data) return;

                // いま開いているチャットの相手は数えない
                var total = data.total;
                if (IGNORE && data.byPartner[IGNORE]) {
                    total -= data.byPartner[IGNORE];
                }

                // 「チャット画面へ」の合計バッジ
                setBadge(document.getElementById("chatUnreadBadge"), total);

                // 相手ごとのバッジ（参加者カードの「チャットする」・チャット一覧）
                document.querySelectorAll("[data-unread-partner]").forEach(function (el) {
                    var id = el.getAttribute("data-unread-partner");
                    setBadge(el, id === IGNORE ? 0 : (data.byPartner[id] || 0));
                });

                // タブのタイトルにも件数（例：(2) 参加者一覧 | Chat2Real）
                document.title = total > 0 ? "(" + total + ") " + baseTitle : baseTitle;

                // 新しいメッセージが来たら通知カード
                var l = data.latest;
                if (l && String(l.partnerId) !== IGNORE && l.messageId > seen) {
                    seen = l.messageId;
                    setSeen(seen);
                    showToast(l);
                }
                lastTotal = total;
            })
            .catch(function () { /* 通信できないときは次の回にまた聞く */ });
    }

    check();
    setInterval(function () {
        if (!document.hidden) check();
    }, 5000);
    document.addEventListener("visibilitychange", function () {
        if (!document.hidden) check();          // 画面に戻ってきたらすぐ確認
    });
})();
</script>
