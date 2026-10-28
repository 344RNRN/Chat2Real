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
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>運営者アカウント作成 | Chat2Real</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/admin-register.css">
</head>
<body>
<%@ include file="/common/header.jsp" %>

<main class="main-container">

    <!-- 左側：システム紹介（ログイン画面と同じ） -->
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

    <!-- 右側：アカウント作成フォーム -->
    <section class="login-section">

        <div class="login-card">

            

            <h2 class="login-title">運営者アカウント作成</h2>

            

<% if (request.getAttribute("errorMessage") != null) { %>
            <div class="error-message" role="alert">
                <%= h(request.getAttribute("errorMessage")) %>
            </div>
<% } %>

            <form action="<%= request.getContextPath() %>/AdminRegister.action" method="post" class="login-form">

                <div class="form-group">
                    <label for="organizerName">運営者名（団体名） ※</label>
                    <div class="input-wrapper">
                        <input type="text" id="organizerName" name="organizerName" maxlength="50" required
                               placeholder="運営者名を入力"
                               value="<%= h(request.getParameter("organizerName")) %>">
                    </div>
                </div>

                <div class="form-group">
                    <label for="email">メールアドレス ※</label>
                    <div class="input-wrapper">
                        <input type="email" id="email" name="email" required autocomplete="email"
                               placeholder="メールアドレスを入力"
                               value="<%= h(request.getParameter("email")) %>">
                    </div>
                </div>

                <!-- パスワードと確認（低い画面では横に並べる） -->
                <div class="form-row">

                    <div class="form-group">
                        <label for="password">パスワード ※</label>
                        <div class="input-wrapper">
                            <input type="password" id="password" name="password" minlength="8" required
                                   pattern="(?=.*[A-Za-z])(?=.*[0-9]).{8,}" autocomplete="new-password"
                                   placeholder="英字と数字を含む8文字以上">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="passwordConfirm">パスワード（確認） ※</label>
                        <div class="input-wrapper">
                            <input type="password" id="passwordConfirm" name="passwordConfirm" minlength="8" required
                                   autocomplete="new-password"
                                   placeholder="もう一度パスワードを入力">
                        </div>
                    </div>

                </div>

                <button type="submit" name="createAccountButton" class="login-button">
                    アカウントを作成
                </button>

            </form>

            <div class="login-footer">
                <a href="<%= request.getContextPath() %>/admin/adminLogin.jsp">ログイン画面へ</a>
                <span>参加者の方は、会場のQRコードから入室してください。</span>
            </div>

        </div>

    </section>

</main>

<%@ include file="/common/footer.jsp" %>
