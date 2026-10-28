<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>Chat2Real 運営 - 交流サポート秋祭り 2026</title>
    <style>
        /* 全体の初期化とフォント設定 */
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: "Helvetica Neue", Arial, "Hiragino Kaku Gothic ProN", "Hiragino Sans", Meiryo, sans-serif; background-color: #f7f7f7; color: #333; font-size: 14px; padding: 10px; }

        /* ヘッダーエリア */
        header { background-color: #221e1f; color: #fff; height: 50px; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
        .header-left { display: flex; align-items: center; gap: 20px; }
        .logo { font-size: 18px; font-weight: bold; color: #fff; }
        .logo span { background-color: #fff; color: #221e1f; padding: 2px 6px; font-size: 12px; margin-left: 5px; border-radius: 2px; }
        .header-title { font-size: 14px; color: #ccc; }
        .header-right { display: flex; align-items: center; gap: 20px; font-size: 12px; }
        .logout-btn { background-color: #4a4a4a; color: #fff; border: none; padding: 6px 15px; font-size: 12px; cursor: pointer; border-radius: 2px; }

        /* メインコンテンツの外枠 */
        .container { max-width: 1200px; margin: 15px auto; background-color: #fff; border: 1px solid #ddd; padding: 20px; position: relative; }

        /* イベント基本情報＆操作エリア */
        .event-trigger-section { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
        .event-info h2 { font-size: 20px; font-weight: bold; margin-bottom: 5px; }
        .event-info p { color: #666; font-size: 12px; }
        .action-buttons { display: flex; align-items: center; border: 1px solid #ccc; border-radius: 4px; overflow: hidden; background: #fff; }
        .status-badge { background-color: #5bb3a2; color: white; padding: 10px 15px; font-weight: bold; font-size: 12px; border-right: 1px solid #ccc; }
        .action-buttons button { background: #fff; color: #333; border: none; padding: 10px 15px; font-size: 12px; font-weight: bold; cursor: pointer; border-right: 1px solid #ccc; }
        .action-buttons button:last-child { border-right: none; }
        .action-buttons button.orange-btn { background-color: #e68543; color: white; border-right: 1px solid #ccc; }
        .action-buttons button.end-btn { color: #d9534f; }

        /* KPIサマリーカード */
        .kpi-row { display: flex; gap: 15px; margin-bottom: 15px; }
        .kpi-card { flex: 1; border: 1px solid #ccc; border-radius: 4px; padding: 12px; background: #fff; text-align: left; box-shadow: inset 0 1px 3px rgba(0,0,0,0.05); }
        .kpi-title { font-size: 11px; color: #888; margin-bottom: 8px; display: block; }
        .kpi-value { font-size: 24px; font-weight: bold; color: #111; text-align: center; display: block; margin-top: 5px; }

        /* アラート看板 */
        .alert-banner { background-color: #fdf3e7; border: 1px solid #f3d1a8; border-radius: 4px; padding: 10px 15px; color: #c05c1d; font-size: 12px; font-weight: bold; margin-bottom: 20px; }

        /* 下段：2列×2行で綺麗に並べるグリッド設定 */
        .grid-container { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; width: 100%; }
        
        /* データカードデザイン */
        .data-card { border: 1px solid #ddd; border-radius: 4px; padding: 18px; background: #fff; min-height: 260px; display: flex; flex-direction: column; justify-content: flex-start; }
        .card-header-title { font-size: 13px; font-weight: bold; margin-bottom: 3px; color: #111; }
        .card-subtitle { font-size: 11px; color: #999; margin-bottom: 15px; }

        /* 横棒グラフのデザイン */
        .chart-row { display: flex; align-items: center; margin-bottom: 8px; font-size: 11px; height: 22px; width: 100%; }
        .chart-label { width: 110px; color: #444; flex-shrink: 0; }
        .chart-bar-wrapper { flex: 1; background: transparent; height: 100%; display: flex; align-items: center; }
        .bar-orange { background-color: #e68543; height: 100%; border-radius: 2px; }
        .bar-green { background-color: #5bb3a2; height: 100%; border-radius: 2px; }
        .chart-value { margin-left: 15px; font-weight: bold; color: #333; width: 30px; text-align: right; flex-shrink: 0; }

        /* アンケート内訳の色分け */
        .bar-light-orange { background-color: #f5b083; height: 100%; border-radius: 2px; }
        .bar-pale-orange { background-color: #fcd4b9; height: 100%; border-radius: 2px; }

        /* タイムラインテーブル */
        .timeline-table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 5px; }
        .timeline-table th { text-align: left; color: #666; font-weight: bold; padding: 8px; background-color: #f9f9f9; border-bottom: 1px solid #ddd; }
        .timeline-table td { padding: 11px 8px; border-bottom: 1px solid #eee; color: #333; }
        .timeline-table tr:last-child td { border-bottom: none; }

        /* フッター */
        footer { text-align: center; color: #aaa; font-size: 11px; margin-top: 20px; padding-top: 10px; border-top: 1px solid #eee; width: 100%; }
    </style>
</head>
<body>

    <!-- ヘッダー -->
    <header>
        <div class="header-left">
            <div class="logo">Chat2Real <span>運営</span></div>
            <div class="header-title">交流サポート秋祭り 2026</div>
        </div>
        <div class="header-right">
            <span>運営者：中山 さん</span>
            <button class="logout-btn">ログアウト</button>
        </div>
    </header>

    <div class="container">

        <!-- イベント情報＆操作ボタン -->
        <div class="event-trigger-section">
            <div class="event-info">
                <h2>交流サポート秋祭り 2026</h2>
                <p>2026/10/18 (日) 19:00〜21:00 みなとホール 3F</p>
            </div>
            <div class="action-buttons">
                <div class="status-badge">開催中</div>
                <button>編集</button>
                <button>アンケート結果</button>
                <button>QRコード</button>
                <button class="orange-btn">お知らせ配信</button>
                <button class="end-btn">イベント終了</button>
            </div>
        </div>

        <!-- KPIカード -->
        <div class="kpi-row">
            <div class="kpi-card"><span class="kpi-title">参加者数</span><span class="kpi-value">56名</span></div>
            <div class="kpi-card"><span class="kpi-title">チャット利用率</span><span class="kpi-value">71%</span></div>
            <div class="kpi-card"><span class="kpi-title">相互承認率</span><span class="kpi-value">48%</span></div>
            <div class="kpi-card"><span class="kpi-title">直接交流成立率</span><span class="kpi-value">38%</span></div>
            <div class="kpi-card"><span class="kpi-title">満足度（平均）</span><span class="kpi-value">4.2</span></div>
        </div>

        <!-- アラート看板 -->
        <div class="alert-banner">
            まだ誰とも交流できていない参加者：12名 ➔「お知らせ配信」で交流タイムを案内しましょう
        </div>
        
                <!-- ページ最下部のフッター -->
        <footer style="text-align: center; color: #aaa; font-size: 11px; margin-top: 20px; padding-top: 10px; border-top: 1px solid #eee; width: 100%;">
            © 2026 Cチーム - Chat2Real
        </footer>
        