-- ========================================
-- meetup_test_data.sql で入れたテストデータを削除する
-- （外部キーの順番：子テーブルから先に消す）
-- ========================================

BEGIN;

DELETE FROM t002_meetup             WHERE exchange_request_id = 9001
                                       OR event_participant_id IN (9001, 9002);
DELETE FROM t004_2_survey_answers   WHERE event_participant_id IN (9001, 9002);
DELETE FROM t012_exchange_requests  WHERE exchange_request_id = 9001;
DELETE FROM t011_messages           WHERE chat_id = 9001;
DELETE FROM t010_chats              WHERE chat_id = 9001;
DELETE FROM t006_events_participant WHERE event_participant_id IN (9001, 9002);
DELETE FROM t007_profiles           WHERE profile_id IN (9001, 9002);
DELETE FROM t005_participant        WHERE participant_id IN (9001, 9002);
DELETE FROM t004_survey_questions    WHERE event_id IN (9001, 9002);
DELETE FROM t013_notifications      WHERE event_id IN (9001, 9002);
DELETE FROM t014_event_materials    WHERE event_id IN (9001, 9002);
DELETE FROM t003_events             WHERE event_id IN (9001, 9002);
DELETE FROM t001_account            WHERE account_id = 9001;

COMMIT;
