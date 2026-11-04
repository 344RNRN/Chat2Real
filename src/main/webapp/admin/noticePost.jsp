<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>お知らせ配信 | Chat2Real</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/admin-login.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/notice-post.css">
</head>

<body>

    <%@ include file="/common/header.jsp" %>


    <!-- メインコンテンツ -->
    <main class="notice-page">

        <!-- タイトルとログアウトを同じ行に配置 -->
        <div class="notice-page-header">

            <h2 class="notice-page-title">
                お知らせ配信
            </h2>

            <div class="logout-area">
                <form action="${pageContext.request.contextPath}/LogoutAction"
                      method="post"
                      onsubmit="return confirm('ログアウトしますか？');">

                    <button type="submit" class="notice-logout-btn">
                        ログアウト
                    </button>
                </form>
            </div>

        </div>

        <!-- フォームと配信履歴 -->
        <div class="notice-container">

            <!-- 左側：お知らせ入力フォーム -->
            <section class="notice-form">

                <form action="${pageContext.request.contextPath}/NoticePostAction"
                      method="post">

                    <!-- 添付ファイルを送信する場合は multipart/form-data を指定 -->

                    <!-- 配信先 -->
                    <div class="form-row">

                        <label class="form-label">
                            配信先 ※
                        </label>

                        <div class="form-content radio-options">

                            <label>
                                <input type="radio"
                                       name="target"
                                       value="all"
                                       checked>
                                参加者全員
                            </label>

                            <label>
                                <input type="radio"
                                       name="target"
                                       value="active">
                                今は交流したくない人のみ
                            </label>

                        </div>
                    </div>

                    <!-- テンプレート選択 -->
                    <div class="form-row">

                        <label for="template" class="form-label">
                            テンプレート
                        </label>

                        <div class="form-content">

                            <select id="template" name="template">
                                <option value="">選択してください（任意）</option>
                                <option value="交流タイムの案内">交流タイムの案内</option>
                                <option value="会場案内">会場案内</option>
                                <option value="イベント終了案内">イベント終了案内</option>
                            </select>

                        </div>
                    </div>

                    <!-- タイトル入力 -->
                    <div class="form-row">

                        <label for="title" class="form-label">
                            タイトル ※
                        </label>

                        <div class="form-content">

                            <input type="text"
                                   id="title"
                                   name="title"
                                   maxlength="100"
                                   required
                                   placeholder="タイトルを入力してください">

                        </div>
                    </div>

                    <!-- テンプレート選択時にタイトルを自動入力 -->
                    <script>
                        const templateSelect =
                            document.getElementById("template");

                        const titleInput =
                            document.getElementById("title");

                        templateSelect.addEventListener("change", function () {
                            if (this.value !== "") {
                                titleInput.value = this.value;
                            }
                        });
                    </script>

                    <!-- 本文入力 -->
                    <div class="form-row">

                        <label for="message" class="form-label">
                            本文 ※
                        </label>

                        <div class="form-content">

                            <textarea id="message"
                                      name="message"
                                      required
                                      placeholder="お知らせの内容を入力してください"></textarea>

                        </div>
                    </div>

                    <!-- 添付ファイル -->
                    <div class="form-row">

                        <label for="attachment" class="form-label">
                            添付
                        </label>

                        <div class="form-content file-row">

                            <input type="file"
                                   id="attachment"
                                   name="attachment">

                        </div>
                    </div>

                    <!-- 配信・キャンセルボタン -->
                    <div class="form-buttons">

                        <button type="submit"
                                class="send-button"
                                onclick="return confirm('お知らせを配信しますか？');">
                            配信する
                        </button>

                        <button type="reset" class="cancel-button">
                            キャンセル
                        </button>

                    </div>

                </form>
            </section>

            <!-- 右側：配信済みのお知らせ -->
            <section class="notice-history">

                <h3>配信済みのお知らせ</h3>

                <table>
                    <thead>
                        <tr>
                            <th>時刻</th>
                            <th>内容</th>
                            <th>添付</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td>14:02</td>
                            <td>会場マップを更新しました</td>
                            <td>画像</td>
                        </tr>

                        <tr>
                            <td>13:30</td>
                            <td>イベントパンフレットを公開しました</td>
                            <td>PDF</td>
                        </tr>

                        <tr>
                            <td>13:10</td>
                            <td>20分後に交流タイムを開始します</td>
                            <td>－</td>
                        </tr>
                    </tbody>
                </table>

            </section>

        </div>
    </main>

    <%@ include file="/common/footer.jsp" %>

</body>
</html>