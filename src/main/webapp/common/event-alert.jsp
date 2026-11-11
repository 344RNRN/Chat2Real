<%@ page pageEncoding="UTF-8" %>
<%--
  参加者画面の「イベント終了」お知らせ（15秒ごとに EventStatus.action に聞く）
    ・終了10分前から：画面の上に「まもなくイベント終了（あと○分）」を出す
    ・終了したら：「イベントが終了しました・アンケートのお願い」を出して、
                  3秒後に「イベントは終了しました」画面（アンケート案内つき）へ自動で移動する
  使い方：common/header-participants.jsp のいちばん下で include しているので、
          参加者のどの画面でも自動で動く（各 JSP に書かなくてよい）
  ※ 終了画面・アンケート・退出・ログイン系の画面では、移動しない
     （どの画面が該当するかは EventEndedFilter の一覧で決まる：サーバーが "stay" で返す）
  ※ 同じ画面に2回 include されても1回しか動かない
  ※ ほかの JSP と名前がぶつからないよう、変数・クラス名は ea で始めている
--%>
<style>
    .ea-toast {
        position: fixed;
        top: 14px;
        left: 50%;
        z-index: 2000;

        display: flex;
        align-items: center;
        gap: 10px;

        width: max-content;
        max-width: calc(100vw - 24px);
        padding: 12px 14px 12px 16px;
        box-sizing: border-box;

        color: #7a3b00;
        background: #fff7e6;
        border: 1px solid #ffd59a;
        border-radius: 14px;
        box-shadow: 0 12px 30px rgba(40, 66, 105, 0.16);

        font-family: "Noto Sans JP", "Yu Gothic", "Meiryo", sans-serif;
        font-size: 14px;
        font-weight: 700;

        transform: translate(-50%, -140%);
        transition: transform 0.3s ease;
    }

    .ea-toast.is-show {
        transform: translate(-50%, 0);
    }

    /* 終了したとき（青＝お知らせ） */
    .ea-toast.is-ended {
        color: #173b63;
        background: #eef4ff;
        border-color: #bcd2f7;
    }
    .ea-toast.is-ended button {
        color: #173b63;
    }

    .ea-toast .ea-icon {
        font-size: 18px;
    }

    .ea-toast button {
        margin-left: 4px;
        padding: 2px 8px;
        color: #7a3b00;
        background: transparent;
        border: none;
        font-size: 18px;
        line-height: 1;
        cursor: pointer;
    }
</style>

<div class="ea-toast" id="eaToast" role="status" aria-live="polite">
    <span class="ea-icon" aria-hidden="true">⏰</span>
    <span id="eaText">まもなくイベント終了です</span>
    <button type="button" id="eaClose" aria-label="閉じる">×</button>
</div>

<script>
(function () {
    if (window.__eaStarted) return;          // 2回 include されても1回だけ
    window.__eaStarted = true;

    var CTX = "<%= request.getContextPath() %>";
    var STATUS_URL = CTX + "/participant/EventStatus.action?path=" + encodeURIComponent(location.pathname);
    var CLOSED_URL = CTX + "/participant/EventClosed.action";

    var toast = document.getElementById("eaToast");
    var icon = toast.querySelector(".ea-icon");
    var text = document.getElementById("eaText");
    var closedByUser = false;
    var ended = false;

    document.getElementById("eaClose").addEventListener("click", function () {
        closedByUser = true;
        toast.classList.remove("is-show");
    });

    function onEnded() {
        if (ended) return;
        ended = true;
        icon.textContent = "🎉";
        text.textContent = "イベントが終了しました。終了アンケートにご協力ください";
        toast.classList.add("is-ended", "is-show");
        setTimeout(function () { location.href = CLOSED_URL; }, 3000);   // 3秒後に終了画面へ
    }

    function check() {
        if (ended) return;
        fetch(STATUS_URL, { cache: "no-store", credentials: "same-origin" })
            .then(function (res) { return res.ok ? res.json() : null; })
            .then(function (s) {
                if (!s) return;
                if (s.ended) {
                    // アンケート・退出・終了画面など、終了後も使う画面ではそのまま（確認もやめる）
                    // ※ サーバーが古くて "stay" が無いときは、URL で判断する（終了画面のくり返し読み込みを防ぐ）
                    var stay = (typeof s.stay === "boolean")
                            ? s.stay
                            : /EventClosed|eventClosed|EventReport|eventReport|SurveyAnswer|SurveyComplete|surveyComplete|Leave|Login|Register/.test(location.pathname);
                    if (stay || /EventClosed|eventClosed/.test(location.pathname)) { ended = true; return; }
                    onEnded();
                    return;
                }
                if (s.minutesLeft >= 0 && s.minutesLeft <= 10) {
                    text.textContent = "まもなくイベント終了です（あと" + Math.max(s.minutesLeft, 1) + "分）";
                    if (!closedByUser) toast.classList.add("is-show");
                }
            })
            .catch(function () { /* 通信できないときは次の回にまた聞く */ });
    }

    check();
    setInterval(function () { if (!document.hidden) check(); }, 15000);
    // スマホでほかのアプリから戻ってきたときも、すぐ確認する
    document.addEventListener("visibilitychange", function () { if (!document.hidden) check(); });
    window.addEventListener("pageshow", function (e) { if (e.persisted) check(); });
})();
</script>
