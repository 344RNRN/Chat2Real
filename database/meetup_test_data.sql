-- ========================================
-- 参加者画面（09 相互承認／10 合流／11 交流結果登録／07 チャット終了／12 イベント資料／13-2 送信完了）
-- と 19 イベント終了確認 の動作確認用テストデータ
-- ※ 先に meetup_add_current_zone.sql を実行しておくこと
-- ※ ID は 9001 番台に固定（本物のデータとぶつからないように）
-- ※ 消すときは meetup_test_data_delete.sql を実行する
--
-- 自分 ＝ event_participant_id 9001（NYI）※ MeetupAction の開発用の仮ID
-- 相手 ＝ event_participant_id 9002（遥）
--
-- 確認URL（http://localhost:8080/Chat2Real に続けて）
--   09 /participant/MutualConsent.action?exchangeRequestId=9001
--   10 /participants/meetup?exchangeRequestId=9001
--   11 /participants/meetResult?exchangeRequestId=9001
--   07 /participant/ChatEnd.action?chatId=9001  （終了するのは POST。下の「テスト用」参照）
--   12 /participant/Materials.action
--   13-2 /participant/SurveyComplete.action
--   19 /admin/EventEnd.action?eventId=9001
-- ========================================

BEGIN;

-- 運営者アカウント（イベントの作成者。ログインには使わない）
INSERT INTO t001_account (account_id, login_id, password_hash, account_name)
OVERRIDING SYSTEM VALUE
VALUES (9001, 'meetup-test@example.com', 'dummy-not-for-login', '合流テスト運営');

-- イベント（9002 は「次回イベント」の選択肢用）
INSERT INTO t003_events (event_id, account_id, event_name, event_datetime, venue, event_status)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, '合流テストイベント', '2026-11-06 18:00', 'テスト会場', '開催中'),
       (9002, 9001, '合流テストイベント 冬', '2026-12-13 18:00', 'テスト会場B', '準備中');

-- 参加者 2人
INSERT INTO t005_participant (participant_id) OVERRIDING SYSTEM VALUE VALUES (9001), (9002);

-- プロフィール
INSERT INTO t007_profiles (profile_id, participant_id, nickname, age_group, gender)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, 'NYI', '20代', '男性'),
       (9002, 9002, '遥',  '20代', '女性');

-- イベント参加者（遥は「ステージ前」で合図を送ったことにする）
INSERT INTO t006_events_participant
    (event_participant_id, event_id, participant_id, profile_id, exchange_status, current_zone)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, 9001, 9001, '交流したい', NULL),
       (9002, 9001, 9002, 9002, '交流したい', 'ステージ前');

-- 2人の個人チャットとメッセージ
INSERT INTO t010_chats (chat_id, event_id, chat_type, chat_status)
OVERRIDING SYSTEM VALUE VALUES (9001, 9001, '個人', '利用中');

INSERT INTO t011_messages (message_id, chat_id, sender_participant_id, message_text)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, 9001, 'はじめまして！'),
       (9002, 9001, 9002, 'よろしくお願いします！');

-- 交流申請（NYI → 遥）。お互いが「直接交流したい」を選んだので「承認」
INSERT INTO t012_exchange_requests
    (exchange_request_id, chat_id, requester_event_participant_id, target_event_participant_id, request_status)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, 9001, 9002, '承認');

-- お知らせ（イベント資料画面に表示）
INSERT INTO t013_notifications (notification_id, event_id, account_id, title, notification_text)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, 9001, '20分後に交流タイムを開始します', 'ステージ前に集まってください。');

-- 資料（ファイル本体は無いので、開くと 404 になる）
INSERT INTO t014_event_materials (material_id, event_id, file_name, file_path, file_type, file_size)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, 'イベントパンフレット.pdf', '/uploads/materials/test-9001.pdf', 'application/pdf', 1024);

-- 満足度アンケート（終了レポート画面 → 送信完了画面の確認用）
INSERT INTO t004_survey_questions (question_id, event_id, question_text, question_type, is_required, display_order)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, '今日のイベントの満足度を教えてください', 'rating', true, 1);

COMMIT;

-- ===== テスト用：画面を「最初の状態」に戻す =====
-- UPDATE t010_chats SET chat_status = '利用中' WHERE chat_id = 9001;
-- UPDATE t003_events SET event_status = '開催中' WHERE event_id = 9001;
-- DELETE FROM t013_notifications WHERE event_id = 9001 AND notification_id <> 9001;
-- DELETE FROM t004_2_survey_answers WHERE event_participant_id IN (9001, 9002);
