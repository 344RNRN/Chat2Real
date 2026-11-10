<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="Bean.NotificationBean" %>
<%!
    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>
<%
    String ctx = request.getContextPath();

    // ===== NoticePostAction から受け取るデータ =====
    Object eventIdObj = request.getAttribute("eventId");
    if (eventIdObj == null) {
        // Action を通さずに開いたときは Action から開き直す
        response.sendRedirect(ctx + "/NoticePostAction");
        return;
    }
    long eventId = ((Number) eventIdObj).longValue();
    String eventName = (String) request.getAttribute("eventName");

    @SuppressWarnings("unchecked")
    List<NotificationBean> notificationList =
            (List<NotificationBean>) request.getAttribute("notificationList");

    // 配信・削除の結果メッセージ
    String okMessage = null;
    String ngMessage = null;
    if ("1".equals(request.getParameter("success"))) {
        okMessage = "お知らせを配信しました。参加者の画面に表示されます。";
    } else if ("success".equals(request.getParameter("delete"))) {
        okMessage = "お知らせを削除しました。";
    } else if ("failed".equals(request.getParameter("delete"))) {
        ngMessage = "お知らせを削除できませんでした。";
    } else if ("empty".equals(request.getParameter("error"))) {
        ngMessage = "タイトルと本文を入力してください。";
    } else if ("length".equals(request.getParameter("error"))) {
        ngMessage = "タイトルは100文字以内で入力してください。";
    } else if ("type".equals(request.getParameter("error"))) {
        ngMessage = "添付できるのは画像またはPDFだけです。";
    } else if ("size".equals(request.getParameter("error"))) {
        ngMessage = "添付ファイルは10MB以内にしてください。";
    }

    SimpleDateFormat timeFormat = new SimpleDateFormat("M/d HH:mm");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>お知らせ配信 | Chat2Real</title>

    <link rel="stylesheet" href="<%= ctx %>/css/admin-login.css">
    <link rel="stylesheet" href="<%= ctx %>/css/notice-post.css">
</head>

<body>

    <%@ include file="/common/header.jsp" %>

    <!-- メインコンテンツ -->
    <main class="notice-page">

        <!-- タイトルとログアウトを同じ行に配置 -->
        <div class="notice-page-header">

            <div>
                <h2 class="notice-page-title">お知らせ配信</h2>
                <p class="notice-event-name">
                    対象イベント：<strong><%= escapeHtml(eventName) %></strong>
                    <a href="<%= ctx %>/admin/EventDashboard.action?eventId=<%= eventId %>"
                       class="dashboard-link">‹ ダッシュボードに戻る</a>
                </p>
            </div>

            <div class="logout-area">
                <form action="<%= ctx %>/LogoutAction"
                      method="post"
                      onsubmit="return confirm('ログアウトしますか？');">
                    <button type="submit" class="notice-logout-btn">ログアウト</button>
                </form>
            </div>

        </div>

        <!-- 配信・削除の結果 -->
        <% if (okMessage != null) { %>
        <p class="notice-message ok" role="status"><%= okMessage %></p>
        <% } else if (ngMessage != null) { %>
        <p class="notice-message ng" role="alert"><%= ngMessage %></p>
        <% } %>

        <!-- フォームと配信履歴 -->
        <div class="notice-container">

            <!-- 左側：お知らせ入力フォーム -->
            <section class="notice-form">

                <form action="<%= ctx %>/NoticePostAction" method="post" enctype="multipart/form-data">

                    <!-- どのイベントに配信するか -->
                    <input type="hidden" name="eventId" value="<%= eventId %>">

                    <!-- 配信先 -->
                    <div class="form-row">
                        <label class="form-label">配信先 ※</label>

                        <div class="form-content radio-options">
                            <label>
                                <input type="radio" name="target" value="all" checked>
                                参加者全員
                            </label>
                            <label>
                                <input type="radio" name="target" value="active">
                                今は交流したくない人のみ
                            </label>
                        </div>
                    </div>

                    <!-- テンプレート選択 -->
                    <div class="form-row">
                        <label for="template" class="form-label">テンプレート</label>

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
                        <label for="title" class="form-label">タイトル ※</label>

                        <div class="form-content">
                            <input type="text"
                                   id="title"
                                   name="title"
                                   maxlength="100"
                                   required
                                   placeholder="タイトルを入力してください">
                        </div>
                    </div>

                    <!-- 本文入力 -->
                    <div class="form-row">
                        <label for="message" class="form-label">本文 ※</label>

                        <div class="form-content">
                            <textarea id="message"
                                      name="message"
                                      required
                                      placeholder="お知らせの内容を入力してください"></textarea>
                        </div>
                    </div>

                    <!-- 添付ファイル（画像・PDF、10MB まで）→ 参加者のお知らせの中に「開く」ボタンが出る -->
                    <div class="form-row">
                        <label for="attachment" class="form-label">添付</label>

                        <div class="form-content file-row">
                            <input type="file" id="attachment" name="attachment"
                                   accept="image/*,application/pdf">
                            <span class="file-note">画像・PDF（10MB まで）</span>
                        </div>
                    </div>

                    <!-- 配信・キャンセルボタン -->
                    <div class="form-buttons">
                        <button type="submit"
                                class="send-button"
                                onclick="return confirm('お知らせを配信しますか？');">
                            配信する
                        </button>

                        <button type="reset" class="cancel-button">キャンセル</button>
                    </div>

                </form>
            </section>

            <!-- 右側：配信済みのお知らせ -->
            <section class="notice-history">

                <h3>配信済みのお知らせ
                    <span class="history-count"><%= notificationList == null ? 0 : notificationList.size() %>件</span>
                </h3>

                <table>
                    <thead>
                        <tr>
                            <th>時刻</th>
                            <th>内容</th>
                            <th>操作</th>
                        </tr>
                    </thead>

                    <tbody>
                    <% if (notificationList != null && !notificationList.isEmpty()) {
                           for (NotificationBean notification : notificationList) { %>
                        <tr>
                            <td class="time-cell">
                                <%= notification.getCreatedAt() == null ? "－"
                                        : timeFormat.format(notification.getCreatedAt()) %>
                            </td>

                            <td>
                                <strong class="history-title"><%= escapeHtml(notification.getTitle()) %></strong>
                                <span class="history-text"><%= escapeHtml(notification.getNotificationText()) %></span>
                            <% if (notification.hasAttachment()) { %>
                                <a class="history-attachment"
                                   href="<%= ctx %><%= escapeHtml(notification.getAttachmentPath()) %>"
                                   target="_blank" rel="noopener">📎 <%= escapeHtml(notification.getAttachmentName()) %></a>
                            <% } %>
                            </td>

                            <td>
                                <form action="<%= ctx %>/NoticeDeleteAction"
                                      method="post"
                                      class="delete-form"
                                      onsubmit="return confirm('このお知らせを削除しますか？\n参加者の画面からも消えます。');">
                                    <input type="hidden" name="notificationId" value="<%= notification.getNotificationId() %>">
                                    <input type="hidden" name="eventId" value="<%= eventId %>">
                                    <button type="submit" class="delete-button">削除</button>
                                </form>
                            </td>
                        </tr>
                    <%     }
                       } else { %>
                        <tr>
                            <td colspan="3" class="history-empty">配信済みのお知らせはありません。</td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>

            </section>

        </div>
    </main>

    <!-- テンプレートを選んだら、タイトル（と本文のひな形）を自動入力 -->
    <script>
        (function () {
            var templateSelect = document.getElementById("template");
            var titleInput = document.getElementById("title");
            var messageInput = document.getElementById("message");

            var bodies = {
                "交流タイムの案内": "まもなく交流タイムです。気になる参加者にチャットで話しかけてみましょう！",
                "会場案内": "休憩スペース・ドリンクコーナーのご案内です。詳しくはイベント資料をご覧ください。",
                "イベント終了案内": "本日はご参加ありがとうございました。終了アンケートへのご協力をお願いします。"
            };

            templateSelect.addEventListener("change", function () {
                if (this.value === "") {
                    return;
                }
                titleInput.value = this.value;
                if (messageInput.value.trim() === "" && bodies[this.value]) {
                    messageInput.value = bodies[this.value];   // 本文が空のときだけ入れる
                }
            });
        })();
    </script>

    <%-- 共通フッター（</body></html> もここで閉じる） --%>
    <%@ include file="/common/footer.jsp" %>
