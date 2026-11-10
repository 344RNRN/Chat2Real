<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="Bean.QRCodeBean" %>
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

    // ===== サーブレット（QRCodeAction）から受け取るデータ =====
    QRCodeBean qr = (QRCodeBean) request.getAttribute("qr");

    // Action を通さずに開いたときは Action から開き直す
    if (qr == null) {
        response.sendRedirect(ctx + "/admin/QRCode.action");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>参加用QRコード | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/qr-code.css">
    <%-- QRコードを作るライブラリ（qrcode-generator / MIT ライセンス）。ネットが無くても動くようにプロジェクトに入れている --%>
    <script src="<%= ctx %>/js/qrcode.js"></script>
</head>
<body class="qr-body">

<%@ include file="/common/header.jsp" %>

<main class="qr-page">

    <!-- 見出し -->
    <div class="qr-head">
        <div>
            <span class="page-eyebrow">QR CODE</span>
            <h1 class="qr-title">参加用QRコード</h1>
        </div>
        <a href="<%= ctx %>/admin/EventDashboard.action?eventId=<%= qr.getEventId() %>" class="back-button">← イベント管理に戻る</a>
    </div>

    <div class="qr-grid">

        <!-- ===== 左：QRコード（印刷するのはここだけ） ===== -->
        <section class="card qr-card" id="printArea">
            <p class="qr-event-name"><%= h(qr.getEventName()) %></p>
            <p class="qr-event-meta"><%= h(qr.getDateText()) %>　<%= h(qr.getVenue()) %></p>

            <div class="qr-image" id="qrImage" role="img" aria-label="参加登録画面のQRコード"></div>

            <p class="qr-caption">スマートフォンのカメラで読み取って、参加登録をしてください</p>
            <p class="qr-url" id="qrUrl"><%= h(qr.getRegisterUrl()) %></p>
        </section>

        <!-- ===== 右：操作・設定 ===== -->
        <section class="card">
            <h2 class="card-title">使い方</h2>

        <% if (qr.isLocalhostOnly()) { %>
            <div class="alert">
                スマホから開けるアドレスが見つかりませんでした（今は localhost になっています）。
                PC を Wi-Fi につないでから、このページを開き直してください。
            </div>
        <% } %>

            <div class="button-stack">
                <button type="button" class="main-button" id="printButton">印刷する</button>
                <a class="sub-button" id="saveButton" download="chat2real-qr-event<%= qr.getEventId() %>.gif">画像を保存</a>
                <button type="button" class="sub-button" id="copyButton">URLをコピー</button>
                <a class="sub-button" href="<%= h(qr.getRegisterUrl()) %>" target="_blank" rel="noopener">参加登録画面を開いて確認</a>
            </div>

            <!-- QRコードに入れるアドレス（PC の IP アドレス） -->
        <% if (qr.getHostOptions().size() > 1) { %>
            <form class="host-form" method="get" action="<%= ctx %>/admin/QRCode.action">
                <input type="hidden" name="eventId" value="<%= qr.getEventId() %>">
                <label for="host">QRコードに入れるアドレス</label>
                <select id="host" name="host" onchange="this.form.submit()">
                <% for (String[] opt : qr.getHostOptions()) { %>
                    <option value="<%= h(opt[0]) %>" <%= opt[0].equals(qr.getSelectedHost()) ? "selected" : "" %>><%= h(opt[1]) %></option>
                <% } %>
                </select>
                <p class="hint">スマホで開けないときは、別のアドレスを選んでみてください（ふつうは Wi-Fi のもの）。</p>
            </form>
        <% } %>

            <h3 class="check-title">スマホで開けないときは</h3>
            <ol class="check-list">
                <li>スマホと PC が<strong>同じ Wi-Fi</strong> につながっているか確認する（スマホのモバイル通信はオフ）。</li>
                <li>Windows のファイアウォールで <strong>Java（Tomcat）</strong>の通信を許可する。初めて起動したときに出る「アクセスを許可する」を押していなければ、
                    「Windows Defender ファイアウォール → アプリに許可」で Java をプライベートネットワークで許可する。</li>
                <li>学校・会社の Wi-Fi は、端末どうしの通信が禁止されていることがある。そのときは PC のテザリングやモバイルルーターを使う。</li>
            </ol>
        </section>

    </div>

</main>

<script>
(function () {
    const url = document.getElementById("qrUrl").textContent.trim();

    // ---------- QRコードを作る ----------
    const area = document.getElementById("qrImage");
    if (typeof qrcode === "undefined") {
        area.textContent = "QRコードを表示できませんでした（js/qrcode.js が見つかりません）";
        return;
    }
    const qr = qrcode(0, "M");      // 0 = 大きさは自動、M = 読み取りやすさ（標準）
    qr.addData(url);
    qr.make();
    area.innerHTML = qr.createSvgTag({ cellSize: 8, margin: 4, scalable: true });

    // 画像として保存（GIF）
    document.getElementById("saveButton").href = qr.createDataURL(10, 4);

    // ---------- 印刷 ----------
    document.getElementById("printButton").addEventListener("click", function () {
        window.print();
    });

    // ---------- URL をコピー ----------
    const copyButton = document.getElementById("copyButton");
    copyButton.addEventListener("click", function () {
        const done = function () {
            copyButton.textContent = "コピーしました";
            setTimeout(function () { copyButton.textContent = "URLをコピー"; }, 2000);
        };
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(url).then(done);
        } else {
            // http（IPアドレス）で開いているときは clipboard API が使えないので古い方法で
            const t = document.createElement("textarea");
            t.value = url;
            document.body.appendChild(t);
            t.select();
            document.execCommand("copy");
            t.remove();
            done();
        }
    });
})();
</script>

<%@ include file="/common/footer.jsp" %>
