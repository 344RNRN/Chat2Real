<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>運営者ログイン | Chat2Real</title>

   <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/admin-login.css?v=2">
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
        </nav>

    </header>


    <!-- Main Content -->
    <main class="main-container">


        <!-- Right: Login Form -->
        <section class="login-section">

            <div class="login-card">

                <div class="lock-icon">
                    🔒
                </div>

                <h2 class="login-title">運営者ログイン</h2>

                <p class="login-description">
                    運営者アカウントでログインしてください。
                </p>

                <!-- Error message -->
                <% if (request.getAttribute("errorMessage") != null) { %>
                    <div class="error-message" role="alert">
                        <%= request.getAttribute("errorMessage") %>
                    </div>
                <% } %>

                <!-- Login Form -->
                <form
                    action="${pageContext.request.contextPath}/admin/login"
                    method="post"
                    class="login-form">

                    <div class="form-group">
                        <label for="mailaddress">メールアドレス</label>

                        <div class="input-wrapper">
         

                            <input
                                type="email"
                                id="mailaddress"
                                name="mailaddress"
                                placeholder="メールアドレスを入力"
                                autocomplete="username"
                                required>
                        </div>
                    </div>


                    <div class="form-group">
                        <label for="password">パスワード</label>

                        <div class="input-wrapper">
                           
                            <input
                                type="password"
                                id="password"
                                name="password"
                                placeholder="パスワードを入力"
                                autocomplete="current-password"
                                required>

                            <button
                                type="button"
                                class="password-toggle"
                                id="togglePassword"
                                aria-label="パスワードを表示"
                                aria-pressed="false">
                                表示
                            </button>
                        </div>
                    </div>


                    <button type="submit" class="login-button">
                        <span class="button-icon">➜</span>
                        ログイン
                    </button>

                </form>

                <div class="login-footer">
                	<a href="${pageContext.request.contextPath}/admin/adminRegister.jsp"
					   class="admin-register-link">
					    ♙運営者アカウント登録へ
					</a>
                    
                </div>

            </div>

        </section>

    </main>


    <!-- Footer -->
    <footer class="footer">
         <p>&copy; 2026 Chat2Real. All Rights Reserved.</p>
    </footer>


    <!-- Password Visibility -->
    <script>
	    const passwordInput = document.getElementById("password");
	    const toggleButton = document.getElementById("togglePassword");
	
	    toggleButton.addEventListener("click", function () {
	        const isPassword = passwordInput.type === "password";
	
	        passwordInput.type = isPassword ? "text" : "password";
	        toggleButton.textContent = isPassword ? "非表示" : "表示";
	
	        toggleButton.setAttribute(
	            "aria-label",
	            isPassword ? "パスワードを隠す" : "パスワードを表示"
	        );
	    });
	</script>


</body>
</html>