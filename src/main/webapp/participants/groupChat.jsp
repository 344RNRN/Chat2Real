<%@ page import="Bean.MessageBean" %>
<%@ page import="java.util.List" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>全体チャット | Chat2Real</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/chat.css">
 
</head>

<body>

<jsp:include page="/common/header-participants.jsp" />

<div class="chat-container">

    <div class="chat-header">
        <h2>全体チャット</h2>
        <p>イベント参加者みんなで交流しましょう！</p>
    </div>

    
	<div class="chat-messages">

    <%
        List<MessageBean> messageList =
            (List<MessageBean>) request.getAttribute("messageList");

        Long participantId =
            (Long) request.getAttribute("participantId");

        if (messageList != null && !messageList.isEmpty()) {

            for (MessageBean message : messageList) {

                boolean isMyMessage =
                    participantId != null &&
                    participantId.longValue()
                    == message.getSenderParticipantId();
    %>

        <div class="message <%= isMyMessage ? "my-message" : "" %>">

            <div class="message-info">
                参加者ID：
                <%= message.getSenderParticipantId() %>
                <%= message.getSentAt() %>
            </div>

            <div class="message-content">
                <%= escapeHtml(message.getMessageText()) %>
            </div>

        </div>

    <%
            }

        } else {
    %>

        <p>まだメッセージはありません。</p>

    <%
        }
    %>

</div>


    <!-- メッセージ送信 -->
    <form action="${pageContext.request.contextPath}/AllChatAction"
          method="post"
          class="chat-input-area">

        <textarea name="message"
                  placeholder="メッセージを入力してください"
                  maxlength="500"
                  required></textarea>

        <button type="submit" class="send-button">
            送信
        </button>

    </form>

</div>

<a href="${pageContext.request.contextPath}/admin/topScreen.jsp" class="back-button">
    イベント画面へ戻る
</a>

<%!
    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
%>

</body>
</html>
```