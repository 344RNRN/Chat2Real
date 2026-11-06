<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    // ===== AdminRegisterAction がセッションに入れた登録内容（1回表示したら消す） =====
    String registeredName = (String) session.getAttribute("registeredName");
    String registeredEmail = (String) session.getAttribute("registeredEmail");
    session.removeAttribute("registeredName");
    session.removeAttribute("registeredEmail");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>アカウント作成完了 | Chat2Real</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/admin-register.css">
</head>
<body>
<%@ include file="/common/header.jsp" %>

<main class="main-container">

    <!-- 左側：システム紹介（アカウント作成画面と同じ） -->
    <section class="intro-section">

        <div class="brand-badge">Chat2Real</div>

        <h2 class="intro-title">イベント交流支援システム</h2>

        <p class="intro-description">
            イベントの管理を、より簡単に、より便利に。
        </p>

        <div class="feature-list">

            <div class="feature-item">
                <div class="feature-icon pink">▦</div>
                <div class="feature-text">
                    <h3>イベント管理</h3>
                    <p>イベントの作成・編集・管理</p>
                </div>
            </div>

            <div class="feature-item">
                <div class="feature-icon blue">♟</div>
                <div class="feature-text">
                    <h3>参加者管理</h3>
                    <p>参加者の確認・管理</p>
                </div>
            </div>

            <div class="feature-item">
                <div class="feature-icon purple">☏</div>
                <div class="feature-text">
                    <h3>交流状況の確認</h3>
                    <p>交流成立状況の確認</p>
                </div>
            </div>

            <div class="feature-item">
                <div class="feature-icon green">▥</div>
                <div class="feature-text">
                    <h3>アンケート結果の閲覧</h3>
                    <p>アンケートの集計・分析</p>
                </div>
            </div>

        </div>

        <div class="decoration decoration-one"></div>
        <div class="decoration decoration-two"></div>

    </section>

    <!-- 右側：作成完了メッセージ -->
    <section class="login-section">

        <div class="login-card">

            <div class="complete-icon">✓</div>

            <h2 class="login-title">アカウント作成完了</h2>

            <p class="complete-message">
                運営者アカウントを作成しました。<br>
                登録したメールアドレスとパスワードでログインしてください。
            </p>

<% if (registeredEmail != null) { %>
            <!-- 登録した内容 -->
            <dl class="complete-info">
                <dt>運営者名（団体名）</dt>
                <dd><%= h(registeredName) %></dd>
                <dt>メールアドレス（ログインID）</dt>
                <dd><%= h(registeredEmail) %></dd>
            </dl>
<% } %>

            <a href="<%= request.getContextPath() %>/admin/adminLogin.jsp" class="login-button complete-button">
                ログイン画面へ
            </a>

        </div>

    </section>

</main>

<%@ include file="/common/footer.jsp" %>

</body>
</html>
