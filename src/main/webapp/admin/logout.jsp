<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/common/header.jsp" %>
<%!
public String h(Object value) {
if (value == null) {
return "";
}
 
    String str = value.toString();
 
    return str.replace("&", "&amp;")
              .replace("<", "&lt;")
              .replace(">", "&gt;")
              .replace("\"", "&quot;")
              .replace("'", "&#39;");
}
 
%>

<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>運営者ログアウト | Chat2Real 運営</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/logout.css">
</head>
<body>
<main class="lo-main">
  <section class="lo-card">
    <h1 class="lo-title">運営者ログアウト</h1>
    <p class="lo-message">ログアウトしました</p>
    <div><a class="lo-link" id="loginButton" href="${pageContext.request.contextPath}/admin/Login">ログイン画面に戻る</a></div>
  </section>
</main>
<%@ include file="/common/footer.jsp" %>
</body>
</html>

<!-- test logout-->