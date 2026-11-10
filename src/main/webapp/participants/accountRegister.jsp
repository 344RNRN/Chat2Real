<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList, java.util.List" %>
<%@ page import="Bean.AccountRegisterBean, Bean.InterestBean" %>
<%!
    private static String h(Object value) {
        if (value == null) {
            return "";
        }
        String s = value.toString();
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&':  sb.append("&amp;");  break;
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
%>
<%
    String ctx = request.getContextPath();

    // Action を通さずに JSP を開いたときは Action から開き直す
    if (request.getAttribute("interestList") == null) {
        response.sendRedirect(ctx + "/participant/AccountRegister.action");
        return;
    }

    AccountRegisterBean reg = (AccountRegisterBean) request.getAttribute("register");   // null = イベントなし
    @SuppressWarnings("unchecked")
    List<InterestBean> interestList = (List<InterestBean>) request.getAttribute("interestList");
    @SuppressWarnings("unchecked")
    List<String> ageGroups = (List<String>) request.getAttribute("ageGroups");
    String errorMessage = (String) request.getAttribute("errorMessage");

    if (ageGroups == null) ageGroups = new ArrayList<>();

    // エラーで戻ってきたときは入力した値を入れ直す
    String vNickname = request.getParameter("nickname");
    String vRealName = request.getParameter("realName");
    String vAge      = request.getParameter("ageGroup");
    String vOther    = request.getParameter("otherInterest");
    String vComment  = request.getParameter("comment");
    if (vAge == null) vAge = "20代";
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>参加者アカウント登録 | Chat2Real</title>
    <link rel="stylesheet" href="<%= ctx %>/css/account-register.css">
    <style>
        /* サーバーからのエラー（account-register.css の .error-msg は最初は非表示のため別にする） */
        .server-error {
            margin: 0 0 18px;
            padding: 12px 14px;
            color: #c62828;
            background: #fff1f2;
            border: 1px solid #fecdd3;
            border-radius: 10px;
            font-size: 13px;
            font-weight: 700;
            line-height: 1.7;
        }
    </style>
</head>
<body>
<%@ include file="/common/header-participants.jsp" %>

<main class="page-layout">
    <section class="event-intro">
        <span class="eyebrow">イベント参加登録</span>
    <% if (reg != null) { %>
        <h1><%= h(reg.getEventName()) %></h1>
        <div class="event-meta">
            <span><%= h(reg.getDateText()) %></span>
            <span><%= h(reg.getVenue()) %></span>
        </div>
    <% } else { %>
        <h1>参加できるイベントがありません</h1>
    <% } %>

        <div class="people-illustration">
            <img src="<%= ctx %>/images/people.png"
                 alt="イベントで楽しく会話する男女4人">
        </div>

        <div class="feature-card">
            <div class="feature">
                <span class="feature-icon">♧</span>
                <div><h2>交流をサポート</h2><p>共通の興味で、自然に会話が広がります</p></div>
            </div>
            <div class="feature">
                <span class="feature-icon">▣</span>
                <div><h2>安心して参加</h2><p>ニックネームで気軽に参加できます</p></div>
            </div>
            <div class="feature">
                <span class="feature-icon">♡</span>
                <div><h2>チャットからリアルな交流</h2><p>チャットを通じて会場での交流を楽しめる</p></div>
            </div>
        </div>
    </section>

    <section class="form-card">
        <div class="form-heading">
            <span class="heading-icon">♙</span>
            <h2>参加者アカウント登録</h2>
            <p>イベントに参加するためのプロフィールを登録してください。</p>
        </div>

    <% if (errorMessage != null) { %>
        <div class="server-error" role="alert"><%= h(errorMessage) %></div>
    <% } %>

    <% if (reg != null) { %>
        <form id="registerForm" action="<%= ctx %>/participant/AccountRegister.action"
              method="POST" enctype="multipart/form-data">

            <!-- どのイベントへの登録か -->
            <input type="hidden" name="eventId" value="<%= reg.getEventId() %>">

            <div class="form-group">
                <label for="photo">プロフィール写真</label>
                <input type="file" id="photo" name="photo" accept="image/jpeg, image/png">
                <div id="photoError" class="error-msg"></div>
            </div>

            <div class="form-row">
                <div class="form-group w-55">
                    <label for="nickname" class="required">ニックネーム</label>
                    <input type="text" id="nickname" name="nickname" maxlength="20"
                           placeholder="例：みお" value="<%= h(vNickname) %>">
                    <div id="nicknameError" class="error-msg"></div>
                </div>
                <div class="form-group w-40">
                    <label for="ageGroup" class="required">年代</label>
                    <select id="ageGroup" name="ageGroup">
                    <% for (String age : ageGroups) { %>
                        <option value="<%= h(age) %>" <%= age.equals(vAge) ? "selected" : "" %>><%= h(age) %></option>
                    <% } %>
                    </select>
                    <div id="ageGroupError" class="error-msg"></div>
                </div>
            </div>

            <div class="form-group">
                <label for="realName" class="required">本名（運営者のみ閲覧可）</label>
                <input type="text" id="realName" name="realName" maxlength="30"
                       placeholder="例：山田 友紀" value="<%= h(vRealName) %>">
                <div id="realNameError" class="error-msg"></div>
            </div>

            <div class="form-group">
                <label class="required">興味・特徴（複数選択可）</label>
                <div class="tag-container">
                <% for (InterestBean tag : interestList) {
                       String tagId = "tag" + tag.getInterestId();
                       boolean checked = reg.getInterestIds().contains(tag.getInterestId()); %>
                    <input type="checkbox" id="<%= tagId %>" name="interestIds"
                           value="<%= tag.getInterestId() %>" class="tag-checkbox" <%= checked ? "checked" : "" %>>
                    <label for="<%= tagId %>" class="tag-label"><%= h(tag.getInterestName()) %></label>
                <% } %>
                </div>

                <div class="custom-interest">
                    <label for="otherInterest">その他（自由記入・「、」で区切って複数可）</label>
                    <input type="text" id="otherInterest" name="otherInterest" maxlength="100"
                           placeholder="例：映画鑑賞、カフェ巡り、料理など" value="<%= h(vOther) %>">
                </div>
                <div id="interestsError" class="error-msg"></div>
            </div>

            <div class="form-group">
                <label for="comment">一言・参加目的</label>
                <input type="text" id="comment" name="comment" maxlength="50"
                       placeholder="例：初参加です、仲間募集中！" value="<%= h(vComment) %>">
                <div id="commentError" class="error-msg"></div>
            </div>

            <div class="info-box">
                写真・本名は運営者のみ閲覧できます。「交流したい」参加者には、ニックネーム・興味・一言のみ公開されます。
            </div>

            <button type="submit" class="btn-submit">登録する <span>›</span></button>
        </form>
        
        <p class="login-switch">
            すでに登録済みの方は
            <a href="<%= ctx %>/participant/Login.action?eventId=<%= reg.getEventId() %>">こちらからログイン</a>
        </p>
    <% } %>
    </section>
</main>

<%@ include file="/common/footer.jsp" %>

<script>
(function () {
    const form = document.getElementById('registerForm');
    if (!form) return;   // イベントが無いとき

    function showError(id, message) {
        const el = document.getElementById(id);
        el.textContent = message;
        el.style.display = 'block';
    }

    form.addEventListener('submit', function (event) {
        let isValid = true;
        document.querySelectorAll('.error-msg').forEach(el => {
            el.style.display = 'none';
            el.textContent = '';
        });

        if (!document.getElementById('nickname').value.trim()) {
            showError('nicknameError', 'ニックネームを入力してください');
            isValid = false;
        }

        if (!document.getElementById('realName').value.trim()) {
            showError('realNameError', '本名を入力してください');
            isValid = false;
        }

        const checkedTags = form.querySelectorAll('input[name="interestIds"]:checked').length;
        const other = document.getElementById('otherInterest').value.trim();
        if (checkedTags === 0 && !other) {
            showError('interestsError', '興味・特徴を1つ以上選ぶか、その他に入力してください');
            isValid = false;
        }

        const photo = document.getElementById('photo').files[0];
        if (photo) {
            if (photo.type !== 'image/jpeg' && photo.type !== 'image/png') {
                showError('photoError', 'プロフィール写真は JPEG か PNG を選んでください');
                isValid = false;
            } else if (photo.size > 5 * 1024 * 1024) {
                showError('photoError', 'プロフィール写真は5MB以内にしてください');
                isValid = false;
            }
        }

        if (!isValid) event.preventDefault();
    });
})();
</script>
</body>
</html>
