-- ========================================
-- 合流画面の動作確認用テストデータ
-- ※ 先に meetup_add_current_zone.sql を実行しておくこと
-- ※ ID は 9001 番台に固定（本物のデータとぶつからないように）
-- ※ 消すときは meetup_test_data_delete.sql を実行する
--
-- 確認URL：http://localhost:8080/Chat2Real/participants/meetup?exchangeRequestId=9001
--   自分 ＝ event_participant_id 9001（NYI）※ MeetupAction の開発用の仮ID
--   相手 ＝ event_participant_id 9002（遥）
-- ========================================

BEGIN;

-- 運営者アカウント（イベントの作成者。ログインには使わない）
INSERT INTO t001_account (account_id, login_id, password_hash, account_name)
OVERRIDING SYSTEM VALUE
VALUES (9001, 'meetup-test@example.com', 'dummy-not-for-login', '合流テスト運営');

-- イベント
INSERT INTO t003_events (event_id, account_id, event_name, event_datetime, venue, event_status)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, '合流テストイベント', '2026-11-06 18:00', 'テスト会場', '開催中');

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

-- 2人のチャット
INSERT INTO t010_chats (chat_id, event_id) OVERRIDING SYSTEM VALUE VALUES (9001, 9001);

-- 交流申請（NYI → 遥）
INSERT INTO t012_exchange_requests
    (exchange_request_id, chat_id, requester_event_participant_id, target_event_participant_id)
OVERRIDING SYSTEM VALUE
VALUES (9001, 9001, 9001, 9002);

COMMIT;
