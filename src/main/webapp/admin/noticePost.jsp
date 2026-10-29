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
	<%@ include file="/common/header.jsp" %>
	<form action="${pageContext.request.contextPath}/LogoutAction"
      method="post"
      style="margin-left: 1110px; margin-right: 20px;"
      onsubmit="return confirm('ログアウトしますか？');">

    <button type="submit"
            style="height: 48px;
                   min-width: 110px;
                   padding: 0 20px;
                   font-size: 16px;
                   font-weight: bold;
                   color: white;
                   background-color: #555;
                   border: none;
                   border-radius: 6px;
                   cursor: pointer;">
        ログアウト
    </button>

</form>
    		
    
     <!-- メインコンテンツ -->
    <main style="padding: 20px; max-width: 1400px; margin: 0 auto; box-sizing: border-box;">

        <h2 style="font-size: 32px; margin: 0 0 20px;">
            お知らせ配信
        </h2>

        <div class="notice-container"
        	style="display: flex !important;
            flex-direction: row !important;
            justify-content: flex-start;
            align-items: flex-start;
            gap: 20px;
            width: 100%;
            box-sizing: border-box;">

            <!-- 左側：お知らせ入力フォーム -->
            <section class="notice-form"
             style="flex: 0 0 55%;
                    width: 55%;
                    min-width: 0;
                    box-sizing: border-box;">

                <form action="${pageContext.request.contextPath}/NoticePostAction"
      				method="post">
                      <!-- enctype="multipart/form-data-->

                    
					<div class="form-row"
     					style="display: flex;
            			align-items: center;
            			margin-bottom: 20px;">

    					<label class="form-label" style="width: 100px; flex-shrink: 0;">
        					配信先 ※
    					</label>

    					<div class="form-content radio-options"
         					style="display: flex; align-items: center; gap: 20px;">

        				<label style="white-space: nowrap;">
            				<input type="radio" name="target" value="all" checked>
            					参加者全員
        					</label>

        				<label style="white-space: nowrap;">
            				<input type="radio" name="target" value="active">
            					今は交流したくない人のみ
        				</label>

    				</div>
				</div>

					
                    <!-- テンプレート選択 -->
				<div class="form-row"
     				style="display: flex; align-items: center; margin-bottom: 20px;">
    				<label for="template" class="form-label"
           				style="width: 100px; flex-shrink: 0;">
        				テンプレート
    				</label>

    				<div class="form-content" style="flex: 1;">
        				<select id="template" name="template"
                				style="width: 220px; box-sizing: border-box;">
            				<option value="">選択してください（任意）</option>
            				<option value="交流タイムの案内">交流タイムの案内</option>
            				<option value="会場案内">会場案内</option>
            				<option value="イベント終了案内">イベント終了案内</option>
        				</select>
    				</div>
				</div>

					<!-- タイトル入力 -->
				<div class="form-row"
     				style="display: flex; align-items: center; margin-bottom: 20px;">
    				<label for="title" class="form-label"
           				style="width: 100px; flex-shrink: 0;">
        				タイトル ※
    				</label>

    				<div class="form-content" style="flex: 1;">
        				<input type="text" id="title" name="title"
               				maxlength="100" required
               				placeholder="タイトルを入力してください"
               				style="width: 100%; height: 38px; padding: 8px;
                      		box-sizing: border-box;">
    				</div>
				</div>

				<script>
    				const templateSelect = document.getElementById("template");
    				const titleInput = document.getElementById("title");

    				templateSelect.addEventListener("change", function () {
        				if (this.value !== "") {
            				titleInput.value = this.value;
        				}
    				});
				</script>

					
                    <div class="form-row"
     					style="display: flex;
            			align-items: center;
            			margin-bottom: 20px;">

    					<label for="message" class="form-label"
           					style="width: 100px; flex-shrink: 0;">
        					本文 ※
    					</label>

    					<div class="form-content" style="flex: 1;">
        					<textarea id="message" name="message"
                  				required
                  				placeholder="お知らせの内容を入力してください"
                  				style="width: 100%;
                         		height: 85px;
                         		box-sizing: border-box;"></textarea>
    					</div>

					</div>

                    <div class="form-row"
     					style="display: flex;
            			align-items: center;
            			margin-bottom: 20px;">

    					<label for="attachment" class="form-label"
           					style="width: 100px; flex-shrink: 0;">
        					添付
    					</label>

    					<div class="form-content file-row"
         					style="display: flex; align-items: center;">
        					<input type="file"
               					id="attachment"
               					name="attachment"
               					style="width: 250px;">
    					</div>

					</div>

                    <div class="form-buttons"
     					style="margin-left: 100px;
            			display: flex;
            			gap: 10px;
            			margin-top: 20px;">
                        <button type="submit" class="send-button"
        					style="background-color: #f28c28;
               				color: white;
               				font-size: 16px;
               				padding: 12px 28px;
               				min-width: 130px;
               				min-height: 48px;
               				border: none;
               				border-radius: 6px;
               				cursor: pointer;"
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
             <section class="notice-history"
             style="flex: 1;
                    min-width: 0;
                    box-sizing: border-box;
                    border: 1px solid #c5bdb5;
                    padding: 16px;
                    background-color: #fff;">

        		<h3>配信済みのお知らせ</h3>

        		<table style="width: 100%; border-collapse: collapse;">
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