<%@ page import="Bean.MessageBean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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

    	<c:choose>

        	<c:when test="${not empty messageList}">

            	<c:forEach var="message" items="${messageList}">

                	<div class="message
                    	${message.senderParticipantId == participantId
                    	? 'my-message' : ''}">

                    	<div class="message-info">
                        	参加者ID：
                        	<c:out value="${message.senderParticipantId}" />

                        	<c:out value="${message.sentAt}" />
                    	</div>

                    	<div class="message-content">
                        	<c:out value="${message.messageText}" />
                    	</div>

                	</div>

            	</c:forEach>

        	</c:when>

        	<c:otherwise>

            	<p>まだメッセージはありません。</p>

        	</c:otherwise>

    	</c:choose>

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

<a href="eventHome.jsp" class="back-button">
    イベント画面へ戻る
</a>

</body>
</html>
```