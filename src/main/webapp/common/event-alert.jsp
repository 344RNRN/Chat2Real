<%@ page pageEncoding="UTF-8" %>
<%--
  参加者画面の「イベント終了」お知らせ（30秒ごとに EventStatus.action に聞く）
    ・終了10分前から：画面の上に「まもなくイベント終了（あと○分）」を出す
    ・終了したら：「イベントは終了しました」画面へ自動で移動する
  使い方：参加者の画面の footer.jsp の include のすぐ上に
          <%@ include file="/common/event-alert.jsp" %>
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
    var STATUS_URL = "<%= request.getContextPath() %>/participant/EventStatus.action";
    var CLOSED_URL = "<%= request.getContextPath() %>/participant/EventClosed.action";

    var toast = document.getElementById("eaToast");
    var text = document.getElementById("eaText");
    var closedByUser = false;

    document.getElementById("eaClose").addEventListener("click", function () {
        closedByUser = true;
        toast.classList.remove("is-show");
    });

    function check() {
        fetch(STATUS_URL, { cache: "no-store", credentials: "same-origin" })
            .then(function (res) { return res.ok ? res.json() : null; })
            .then(function (s) {
                if (!s) return;
                if (s.ended) {
                    location.href = CLOSED_URL;          // 終了 → 終了画面へ
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
    setInterval(check, 30000);
})();
</script>
