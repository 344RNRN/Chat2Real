<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    private static String h(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    String ctx = request.getContextPath();
    String eventName = (String) request.getAttribute("eventName");
    if (eventName == null) {
        // Action を通さずに開いたときは Action から開き直す
        response.sendRedirect(ctx + "/participant/EventClosed.action");
        return;
    }
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>イベント終了 | Chat2Real</title>
    <style>
        body.event-closed-screen {
            color: #233b59;
            font-family: "Noto Sans JP", "Yu Gothic", "Meiryo", sans-serif;
            background:
                radial-gradient(circle at 6% 18%, rgba(211, 199, 245, 0.24) 0 82px, transparent 83px),
                radial-gradient(circle at 96% 20%, rgba(255, 207, 225, 0.24) 0 125px, transparent 126px),
                linear-gradient(120deg, #f5f8ff 0%, #ffffff 48%, #fff5f9 100%);
        }

        .closed-page {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 48px 16px;
            box-sizing: border-box;
        }

        .closed-card {
            width: min(100%, 480px);
            padding: 40px 28px 32px;
            box-sizing: border-box;

            text-align: center;
            background: rgba(255, 255, 255, 0.97);
            border: 1px solid #dce5f1;
            border-radius: 22px;
            box-shadow: 0 18px 50px rgba(40, 66, 105, 0.08);
        }

        .closed-icon {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 80px;
            height: 80px;
            font-size: 36px;
            background: linear-gradient(135deg, #fff0f6 0%, #ffe3ee 100%);
            border-radius: 50%;
        }

        .closed-eyebrow {
            display: inline-block;
            margin-top: 16px;
            padding: 5px 13px;
            color: #f45b91;
            background: #fff0f6;
            border-radius: 999px;
            font-size: 11px;
            font-weight: 800;
            letter-spacing: 2px;
        }

        .closed-card h1 {
            margin: 10px 0 6px;
            color: #173b63;
            font-size: 24px;
            font-weight: 800;
        }

        .closed-event {
            margin: 0 0 14px;
            color: #7184a0;
            font-size: 14px;
        }

        .closed-text {
            margin: 0 0 26px;
            color: #233b59;
            font-size: 14px;
            line-height: 1.9;
        }

        .closed-actions {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .closed-primary,
        .closed-secondary {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 50px;
            border-radius: 11px;
            font-size: 15px;
            font-weight: 800;
            text-decoration: none;
        }

        .closed-primary {
            color: #ffffff;
            background: linear-gradient(120deg, #ff7eaa 0%, #f44780 100%);
            box-shadow: 0 8px 18px rgba(244, 91, 145, 0.22);
        }

        .closed-primary:hover {
            filter: brightness(1.03);
        }

        .closed-secondary {
            color: #173b63;
            background: #ffffff;
            border: 1px solid #dce5f1;
        }

        .closed-secondary:hover {
            color: #f45b91;
            border-color: #ffd0e1;
        }

        .closed-note {
            margin: 18px 0 0;
            color: #7184a0;
            font-size: 12px;
            line-height: 1.7;
        }
    </style>
</head>
<body class="event-closed-screen">

<%@ include file="/common/header-participants.jsp" %>

<main class="closed-page">
    <section class="closed-card">
        <span class="closed-icon" aria-hidden="true">🎉</span><br>
        <span class="closed-eyebrow">THANK YOU</span>
        <h1>イベントは終了しました</h1>
        <p class="closed-event"><%= h(eventName) %></p>

        <p class="closed-text">
            本日はご参加いただき、ありがとうございました。<br>
            チャット・参加者一覧などの機能は終了しました。
        </p>

        <div class="closed-actions">
            <a href="<%= ctx %>/participant/EventReport.action" class="closed-primary">終了アンケートに答える</a>
            <a href="<%= ctx %>/participant/Leave.action" class="closed-secondary">退出する</a>
        </div>

        <p class="closed-note">アンケートは1分ほどで終わります。今後のイベント改善に使わせていただきます。</p>
    </section>
</main>

<%-- 共通フッター（</body></html> もここで閉じる） --%>
<%@ include file="/common/footer.jsp" %>
