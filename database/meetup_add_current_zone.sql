-- ========================================
-- 合流画面：参加者の「今いるエリア」を保存する列を追加する
-- （チームで了承済み 2026/11/06）
--
-- 値：入口付近 / ステージ前 / 中央テーブル / ドリンクコーナー
-- まだ合図を送っていない人は NULL
-- 元に戻すときは meetup_rollback_current_zone.sql を実行する
-- ========================================

ALTER TABLE public.t006_events_participant
    ADD COLUMN IF NOT EXISTS current_zone character varying(30);
