<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> 
<%! 
	private static String h(Object value) {
		if (value == null) { 
			return "";
			} 
			String s = value.toString();
			StringBuilder sb = new StringBuilder (s.length());
			for (char c : s.toCharArray()) { 
				switch (c){ 
				 case '&' : sb.append("&amp;");break; 
				 case '<' : sb.append("&lt;"); break; 
				 case '>' : sb.append("&gt;"); break; 
				 case '"' : sb.append("&quot;");break; 
				 case '\'': sb.append("&#39;"); break;
				 default: sb.append(c);
				 }
				}
				return sb.toString();
			} 
	%>
	<% 
		// ==== サーブレットから受け取るデータ（まだ無いときは空にする）====
		String eventName = (String) request.getAttribute("eventName");
		String eventDate = (String) request.getAttribute("eventDate");
		String venue = (String) request.getAttribute("venue");
		Integer participantCount = (Integer) request.getAttribute("participantCount");
		Integer wantCount = (Integer) request.getAttribute("wantCount");
		String noticeText = (String) request.getAttribute("noticeText");
		String noticeTime = (String) request.getAttribute("noticeTime");
		String exchangeStatus = (String) request.getAttribute("exchangeStatus");
		String displayName = (String) request.getAttribute("displayName");
		
		
		
		if (eventName==null) eventName = "";
		if (eventDate==null) eventDate = "－";
		if (venue== null) venue ="－";
		if (participantCount ==null) participantCount=0;
		if (wantCount==null) wantCount = 0; 
		if(displayName == null) displayName = "nickname" ;
		
		
		boolean isWant = "交流したい".equals(exchangeStatus);
		boolean isNotNow= "今は交流したくない".equals(exchangeStatus);
		String ctx = request.getContextPath() ;
		
		%>
		
		
	<!DOCTYPE html > 
	<html lang="ja"> 
	<head> 
		<meta charset="UTF-8"> 
		<meta name="viewport" content = "width=device-width, initial-scale=1" > 
		<title>イベントルーム</title>
		<link rel="stylesheet" href="<%= ctx %>/css/top.css">
		
 	</head>
 	<body>
 		<div class="app">
 		<!-- アプリバー（共通部：画面タイトル。サブタイトル) -->
 		<header class="app-bar">
 			<h1 class="app-title">イベントルーム</h1>
 			<p class="app-sub"><%= h(eventName) %> </p>
 			</header>
 			
 			
 			<main class="content">
 			
 			
 				<!-- 1.お知らせバナー（お知らせがある時だけ表示） -->
 		<% if(noticeText !=null ) {  %>
 			<a class="notice-banner" href="<%= ctx %>/participant/Materials.action">
 				お知らせ：<%= h(noticeText) %>（運営者・<%= h(noticeTime) %>）
 				</a>
 		<%} %>
 				<!-- 2~5. イベント情報 -->
 				<section class="event-card">
 				 <h2 class="event-name"><%= h(eventName) %></h2>
 				 <p class="event-info"><%= h(eventDate) %></p>
 				 <p class="event-info"><%= h(venue) %></p>
 				 <p class="event-info">参加者<%= participantCount %>名（交流したい <%= wantCount %>名）</p>
 				 </section>
 				 
 				 <!-- 6~9 今の気分（交流したい/今は交流したくない） -->
 				 <section class="mood">
 				  <p class="section-title">今の気分を選んでください</p>
 				  	 <form action="<%= ctx  %>/participant/ChangeStatus.action" method="post" class="mood-buttons">
 				  	 	<button type="submit" name="wantButton" value="交流したい" 
 				  	 		class="mood-button<%= isWant ? " selected": "" %>">交流したい</button>
 				  	 	<button type="submit" name="notNowButton" value="今は交流したくない"
 				  	 		class="mood-button<%= isNotNow ? " selected" : "" %>">今は交流したくない</button>
 				  	 		</form>
 				  	 <p class="mood-note">
 				  	 	「交流したい」を選ぶと参加者一覧とチャットが使えます。「今は交流したくない」を選ぶと運営者からの資料が表示されます。いつでも切り替えられます。
 				  	 </p>
 				  	 <p class="current-status">現在：
 		<% if (isWant) { %>
 							<span class="status want">交流したい</span>
 		<% } else if (isNotNow) { %>
 							<span class="status not-now">今は交流したくない</span>
 		<% } else{ %>	
 					<span class="status none">未選択</span>
 		<% } %>
 					</p>
 				</section>
 				
 				
 				<!-- 10. 表示名切り替え（ニックネーム/本名） -->
 				<section class="name-switch">
 				 <p class="section-title">他の参加者に表示する名前</p>
 				 
 				 <form action="<%= ctx  %>/participant/ChangeDisplayName.action" method ="post" class="name-buttons"> 
 				 			<button type="submit" name="displayName" value="nickname"
 				 					class="name-button<%= "nickname".equals(displayName) ? " selected": "" %>">ニックネーム</button>
 				 			<button type="submit" name="displayName" value="realName"
                        class="name-button<%= "realName".equals(displayName) ? " selected" : "" %>">本名</button>
            </form>
        </section>

 			</main>

 			<!-- 11. 下のリンク（イベント資料・退出） -->
 			<nav class="bottom-links">
 				<a href="<%= ctx %>/participant/Materials.action">イベント資料</a>
 				<a href="<%= ctx %>/participant/Leave.action" class="leave-link"
 				   onclick="return confirm('イベントから退出しますか？');">退出</a>
 			</nav>

 		</div>

 	</body>
	</html>
	