<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>お知らせ配信 | Chat2Real</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/admin-login.css">
</head>
<body>
	<!-- Header -->
    <header class="header">

        <div class="header-left">
            <a href="#" class="logo">
                Chat2<span>Real</span>
            </a>

            <div class="header-divider"></div>

            <h1 class="system-name">～チャットからリアルな交流へ</h1>
        </div>

        <nav class="header-nav">
           
            <span class="admin-label">
                <span>●</span> 運営者専用
            </span>
            <form action="${pageContext.request.contextPath}/LogoutAction"method="post"onsubmit="return confirm('ログアウトしますか？');">

    		<button type="submit">ログアウト</button>
        </nav>
        

</form>

    </header>

</body>
</html>