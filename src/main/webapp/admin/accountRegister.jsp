<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>アカウント登録画面</title>
    <style>
        /* 全体の背景とベーススタイル（画像のような暖かみのある薄いグレー/ベージュ） */
        body { 
            font-family: 'Helvetica Neue', Arial, 'Hiragino Kaku Gothic ProN', sans-serif; 
            background-color: #F4F1EA; 
            margin: 0;
            padding: 0;
            display: flex; 
            justify-content: center; 
            color: #333333;
        }
        
        /* スマホ用コンテナ（画面イメージのカード部分） */
        .container { 
            width: 100%; 
            max-width: 414px; 
            background: #F4F1EA; 
            padding: 16px; 
            box-sizing: border-box; 
        }

        /* 各入力グループの余白 */
        .form-group { 
            margin-bottom: 20px; 
        }
        
        /* 2カラム並び（ニックネームと年代） */
        .form-row { 
            display: flex; 
            justify-content: space-between; 
            gap: 12px;
        }
        .w-55 { width: 55%; }
        .w-40 { width: 40%; }

        /* ラベルのスタイル（画像の文字に寄せて少し小さめ太字） */
        label { 
            font-weight: bold; 
            font-size: 13px; 
            display: block; 
            margin-bottom: 6px; 
            color: #4A4A4A;
        }
        
        /* 必須項目の「※」マーク */
        .required::after { 
            content: " ※"; 
            color: #E74C3C; 
        }

        /* 入力フォームのボーダーや背景色を画像に極限まで寄せる */
        input[type="text"], select, textarea { 
            width: 100%; 
            padding: 10px 12px; 
            box-sizing: border-box; 
            border: 1px solid #C8C7CC; 
            border-radius: 4px; 
            background-color: #FFFFFF;
            font-size: 14px;
            color: #333333;
            outline: none;
        }
        
        input[type="text"]:focus, select:focus, textarea:focus {
            border-color: #E67E22; /* フォーカス時はオレンジ */
        }

        /* 興味・特徴タグのオレンジ選択スタイル */
        .tag-container { 
            display: flex; 
            flex-wrap: wrap; 
            gap: 8px; 
            margin-top: 6px;
        }
        .tag-label { 
            background-color: #FFFFFF;
            border: 1px solid #C8C7CC; 
            padding: 6px 14px; 
            border-radius: 20px; /* 丸みのあるタブ */
            cursor: pointer; 
            font-size: 13px; 
            color: #555555;
            transition: all 0.2s;
        }
        .tag-checkbox { display: none; }
        /* 選択されたら画像通りの鮮やかなオレンジに */
        .tag-checkbox:checked + .tag-label { 
            background-color: #E67E22; 
            color: #FFFFFF; 
            border-color: #E67E22; 
            font-weight: bold;
        }
        
        /* No.9 主宰者からの質問エリアの見出しリボン */
        .section-title-bar {
            background-color: #FADBD8; /* 画像のような薄いピンクオレンジ・ベージュの帯 */
            color: #E67E22; /* オレンジの文字色 */
            padding: 6px 12px;
            font-size: 14px;
            font-weight: bold;
            border-radius: 20px 4px 4px 20px;
            margin: 24px 0 16px 0;
            display: flex;
            align-items: center;
        }

        /* 質問文のテキストスタイル */
        .question-label {
            font-size: 13px;
            font-weight: bold;
            color: #4A4A4A;
            margin-bottom: 6px;
            margin-top: 12px;
        }

        /* No.13 注意書きエリア（枠線付きの白いボックス） */
        .info-box {
            background-color: #FFFFFF;
            border: 1px solid #C8C7CC;
            border-radius: 4px;
            padding: 12px;
            font-size: 11px;
            color: #7F8C8D;
            line-height: 1.6;
            margin-top: 24px;
            margin-bottom: 20px;
        }
        
        /* No.14 確認画面へボタン（画像と全く同じオレンジ色とサイズに） */
        .btn-submit { 
            width: 100%; 
            height: 48px; 
            background-color: #E67E22; /* 画像の鮮やかなオレンジ */
            color: #FFFFFF; 
            border: none; 
            border-radius: 4px; 
            font-size: 16px; 
            font-weight: bold; 
            cursor: pointer; 
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            transition: background-color 0.2s;
        }
        .btn-submit:hover {
            background-color: #D35400; /* ホバー時は少し濃く */
        }

        /* エラーメッセージの装飾 */
        .error-msg { 
            color: #E74C3C; 
            font-size: 11px; 
            margin-top: 4px; 
            display: none; 
            font-weight: bold;
        }
    </style>
</head>
<body>

<div class="container">
    
    <!-- ヘッダー（仮のイベント名表示エリア） -->
    <div style="margin-bottom: 20px;">
        <h1 style="font-size: 18px; margin: 0 0 4px 0; color: #333;">交流サポート秋祭り 2026</h1>
        <p style="font-size: 12px; margin: 0; color: #666;">2026年10月18日(日) 19:00 〜 みなとホール</p>
    </div>
    
    <form id="registerForm" action="confirm.jsp" method="POST">
        
        <!-- No.3 プロフィール写真 -->
        <div class="form-group">
            <label>プロフィール写真</label>
            <input type="file" id="photo" name="photo" accept="image/jpeg, image/png">
            <div id="photoError" class="error-msg"></div>
        </div>
        
        <div class="form-row">
            <!-- No.4 ニックネーム -->
            <div class="form-group w-55">
                <label for="nickname" class="required">ニックネーム</label>
                <input type="text" id="nickname" name="nickname" placeholder="みお" value="みお">
                <div id="nicknameError" class="error-msg"></div>
            </div>
            
            <!-- No.5 年代 -->
            <div class="form-group w-40">
                <label for="ageGroup" class="required">年代</label>
                <select id="ageGroup" name="ageGroup">
                    <option value="20" selected>20代</option>
                    <option value="10">10代</option>
                    <option value="30">30代</option>
                    <option value="40">40代</option>
                    <option value="50">50代</option>
                    <option value="60">60代以上</option>
                </select>
                <div id="ageGroupError" class="error-msg"></div>
            </div>
        </div>
        
        <!-- No.6 本名 -->
        <div class="form-group">
            <label for="realName" class="required">本名（運営者のみ閲覧可）</label>
            <input type="text" id="realName" name="realName" value="山田 友紀">
            <div id="realNameError" class="error-msg"></div>
        </div>
        
        <!-- No.7 興味・特徴 -->
        <div class="form-group">
            <label class="required">興味・特徴（複数選択可）</label>
            <div class="tag-container">
                <input type="checkbox" id="tag1" name="interests" value="登山" class="tag-checkbox" checked>
                <label for="tag1" class="tag-label">登山</label>
                
                <input type="checkbox" id="tag2" name="interests" value="旅行" class="tag-checkbox" checked>
                <label for="tag2" class="tag-label">旅行・お出かけ</label>
                
                <input type="checkbox" id="tag3" name="interests" value="インドア" class="tag-checkbox">
                <label for="tag3" class="tag-label">インドア派</label>
            </div>
            <div id="interestsError" class="error-msg"></div>
        </div>
        
        <!-- No.8 一言・参加目的 -->
        <div class="form-group">
            <label for="comment">一言・参加目的</label>
            <input type="text" id="comment" name="comment" value="初参加です、仲間募集中！">
            <div id="commentError" class="error-msg"></div>
        </div>
        
        <!-- No.9 主宰者からの質問エリア見出し -->
        <div class="section-title-bar">
            主宰者からの質問
        </div>
        
        <!-- Q1. No.10 質問文 & No.11 回答（選択式） -->
        <div class="form-group">
            <div class="question-label">Q1. 今日いちばん話したいテーマは？</div>
            <select id="answer1" name="answer1">
                <option value="仕事・キャリア" selected>仕事・キャリア</option>
                <option value="趣味・プライベート">趣味・プライベート</option>
                <option value="その他">その他</option>
            </select>
        </div>
        
        <!-- Q2. No.10 質問文 & No.12 回答（自由記述） -->
        <div class="form-group">
            <div class="question-label">Q2. 今日の参加目的を教えてください</div>
            <input type="text" id="answer2" name="answer2" placeholder="例：同じ趣味の人と話したい">
        </div>
        
        <!-- No.13 注意書き -->
        <div class="info-box">
            写真・本名は運営者のみ閲覧できます。「交流したい」参加者には、ニックネーム・興味・一言のみ公開されます。
        </div>
        
        <!-- No.14 確認画面へボタン -->
        <button type="submit" class="btn-submit">確認画面へ</button>
        
    </form>
</div>

<script>
// フロント側でのバリデーション（イベント処理5）
document.getElementById('registerForm').addEventListener('submit', function(event) {
    let isValid = true;
    
    // エラー初期化
    document.querySelectorAll('.error-msg').forEach(el => el.style.display = 'none');
    
    // ニックネーム必須チェック
    const nickname = document.getElementById('nickname').value.trim();
    if (!nickname) {
        document.getElementById('nicknameError').innerText = "ニックネームを入力してください";
        document.getElementById('nicknameError').style.display = "block";
        isValid = false;
    }
    
    // 本名必須チェック
    const realName = document.getElementById('realName').value.trim();
    if (!realName) {
        document.getElementById('realNameError').innerText = "本名を入力してください";
        document.getElementById('realNameError').style.display = "block";
        isValid = false;
    }

    if (!isValid) {
        event.preventDefault(); // エラーがあれば送信を止める
    }
});
</script>

</body>
</html>
