<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>イベントダッシュボード | Chat2Real</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/event-dashboard.css?v=1">
</head>

<body>

    <!-- Common header -->
    <%@ include file="/common/header.jsp" %>

    <main class="page dashboard">

        <!-- Page navigation (dashboard only) -->
        <div class="event-bar">
            <div class="event-bar-inner">

                <nav aria-label="運営メニュー">
                    <ul class="event-nav">
                        <li><a href="#" class="is-active" aria-current="page">ダッシュボード</a></li>
                        
                    </ul>
                </nav>

                <div class="operator">
                    <span>運営者：<strong>中山</strong> さん</span>

                    <form action="${pageContext.request.contextPath}/admin/logout"
                          method="post">
                        <button type="submit" class="logout-btn">ログアウト</button>
                    </form>
                </div>

            </div>
        </div>


        <div class="container">

            <!-- Event info & actions -->
            <section class="panel event-head">

                <div class="event-info">
                    <h2>
                        交流サポート秋祭り 2026
                        <span class="status-badge">開催中</span>
                    </h2>
                    <p>2026/10/18 (日) 19:00〜21:00　みなとホール 3F</p>
                </div>

                <div class="action-buttons">
                    <button type="button" class="btn">編集</button>
                    <button type="button" class="btn">アンケート結果</button>
                    <button type="button" class="btn">QRコード</button>
                    <button type="button" class="btn btn-primary">お知らせ配信</button>
                    <button type="button" class="btn btn-danger">イベント終了</button>
                </div>

            </section>


            <!-- KPI cards -->
            <section class="kpi-row" aria-label="イベント指標">
                <div class="panel kpi-card">
                    <span class="kpi-title">参加者数</span>
                    <span class="kpi-value">56<span class="kpi-unit">名</span></span>
                </div>
                <div class="panel kpi-card">
                    <span class="kpi-title">チャット利用率</span>
                    <span class="kpi-value">71<span class="kpi-unit">%</span></span>
                </div>
                <div class="panel kpi-card">
                    <span class="kpi-title">相互承認率</span>
                    <span class="kpi-value">48<span class="kpi-unit">%</span></span>
                </div>
                <div class="panel kpi-card">
                    <span class="kpi-title">直接交流成立率</span>
                    <span class="kpi-value">38<span class="kpi-unit">%</span></span>
                </div>
                <div class="panel kpi-card">
                    <span class="kpi-title">満足度（平均）</span>
                    <span class="kpi-value">4.2<span class="kpi-unit">/ 5</span></span>
                </div>
            </section>


            <!-- Alert -->
            <div class="alert-banner" role="status">
                <span class="alert-icon">⚠</span>
                <span>まだ誰とも交流できていない参加者：<strong>12名</strong></span>
                <span>➔「お知らせ配信」で交流タイムを案内しましょう</span>
            </div>

            <!--
                Charts / timeline go here, e.g.:
                <section class="grid-container">
                    <div class="panel data-card"> ... </div>
                </section>
                (styles for .grid-container, .data-card, .chart-row,
                 .timeline-table are already in event-dashboard.css)
            -->

        </div>

    </main>

    <!-- Common footer (closes </body></html>) -->
    <%@ include file="/common/footer.jsp" %>
