<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ include file="/common/header-participants.jsp" %>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>参加者一覧 | Chat2Real</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/participant-list.css">
</head>

<body>

<main class="participant-page">

    <!-- Page Header -->
    <section class="page-heading">
        <div>
            <span class="page-eyebrow">COMMUNITY</span>
            <h1>参加者一覧</h1>
            <p class="page-description">
                気になる参加者を見つけて、チャットで交流を始めましょう。
            </p>
        </div>

        <div class="participant-count">
            <span class="count-number">${ParticipantCount}</span>
            <span class="count-label">交流したい参加者</span>
        </div>
    </section>


    <!-- Search Section -->
    <section class="search-panel">

        <form action="#" method="get" class="search-form">

            <label for="participantName" class="search-label">
                参加者を検索
            </label>

            <div class="search-input-group">
                <input
                    type="text"
                    id="participantName"
                    name="keyword"
                    placeholder="ニックネームまたは名前を入力してください">

                <button type="submit" class="search-button">
                    検索
                </button>
            </div>

        </form>


        <!-- Filter Section -->
        <div class="filter-section">

            <div class="filter-group">
                <span class="filter-label">性別</span>

                <select id="genderFilter" class="filter-select">
                    <option value="">すべて</option>
                    <option value="female">女性</option>
                    <option value="male">男性</option>
                    
                </select>
            </div>


            <div class="filter-group hobby-filter">
                <label for="hobbyFilter" class="filter-label">
                    趣味
                </label>

                <select id="hobbyFilter" class="filter-select">
                    <option value="">すべて</option>
                    <option value="travel">旅行</option>
                    <option value="music">音楽</option>
                    <option value="movie">映画</option>
                    <option value="photo">写真</option>

                </select>
            </div>

        </div>

    </section>


    <!-- Participant List -->
    <section class="participant-section">

        <div class="list-heading">
            <h2>交流できる参加者</h2>
            <span class="list-description">
                気になる相手を選んでみましょう
            </span>
        </div>


        <div class="participant-list">


            <!-- Participant Card 1 -->
            <article class="participant-card">

                <div class="participant-avatar avatar-orange">
                    <span>遥</span>
                </div>

                <div class="participant-information">

                    <div class="participant-name-row">
                        <h3>遥 <span class="reading">（はるか）</span></h3>
                    </div>

                    <div class="hobby-tags">
                        <span class="hobby-tag">登山</span>
                        <span class="hobby-tag">写真</span>
                    </div>

                    <p class="participant-introduction">
                        毎月山に登っています
                    </p>

                </div>

                <div class="participant-action">
                    <a href="#" class="chat-button">
                        チャットする
                    </a>
                </div>

            </article>


            


        </div>


        <!-- Empty State -->
        <div class="empty-message" hidden>
            <div class="empty-icon">⌕</div>
            <h3>参加者が見つかりません</h3>
            <p>検索条件を変更して、もう一度お試しください。</p>
        </div>


	     <div class="list-footer">
			    <p>ほかにも交流できる参加者が ${otherParticipantCount} 名います</p>
		</div>

    </section>


    <!-- Notice -->
    <div class="participant-notice">
        <span class="notice-icon">i</span>
        <p>
            交流したい参加者のみ表示しています。
            気になる相手のカードからチャットを開始できます。
        </p>
    </div>
    
     <!-- Event Materials -->
	<section class="event-materials">
	    <a href="${pageContext.request.contextPath}/eventMaterials.action"
	       class="event-materials-link">
	        イベント資料
	        <span class="link-arrow">→</span>
	    </a>
	</section>
    
</main>



<%@ include file="/common/footer.jsp" %>

</body>
</html>