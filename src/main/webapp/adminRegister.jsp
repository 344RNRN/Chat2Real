<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common/header.jsp" %>

<main class="page">

    <section class="card">
        <h1 class="card-title">運営者アカウント作成</h1>
        <p class="card-lead">イベントの作成・管理を行う運営者のアカウントを作成します</p>

<% if (request.getAttribute("errorMessage") != null) { %>
        <p class="error-message"><%= h(request.getAttribute("errorMessage")) %></p>
<% } %>

        <form action="<%= request.getContextPath() %>/AdminRegister.action" method="post">
            <div class="form-item">
                <label for="organizerName">運営者名（団体名） <span class="required">※</span></label>
                <input type="text" id="organizerName" name="organizerName" maxlength="50" required
                       value="<%= h(request.getParameter("organizerName")) %>">
            </div>

            <div class="form-item">
                <label for="email">メールアドレス <span class="required">※</span></label>
                <input type="email" id="email" name="email" required autocomplete="email"
                       value="<%= h(request.getParameter("email")) %>">
            </div>

            <div class="form-item">
                <label for="password">パスワード <span class="required">※</span>（8文字以上）</label>
                <input type="password" id="password" name="password" minlength="8" required
                       pattern="(?=.*[A-Za-z])(?=.*[0-9]).{8,}" autocomplete="new-password"
                       title="8文字以上で、英字と数字を含めてください">
            </div>

            <div class="form-item">
                <label for="passwordConfirm">パスワード（確認） <span class="required">※</span></label>
                <input type="password" id="passwordConfirm" name="passwordConfirm" minlength="8" required
                       autocomplete="new-password">
            </div>

            <button type="submit" name="createAccountButton" class="btn-primary">アカウントを作成</button>
        </form>

        <p class="card-links"><a href="<%= request.getContextPath() %>/login.jsp">ログイン画面へ</a></p>
        <p class="card-note">参加者の方は、会場のQRコードから入室してください。</p>
    </section>
</main>

<%@ include file="common/footer.jsp" %>
