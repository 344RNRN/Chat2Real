-- ========================================
-- 【元に戻す用】meetup_add_current_zone.sql で追加した列を削除する
-- ※ 保存されていたエリアの値も消えます
-- ========================================

ALTER TABLE public.t006_events_participant
    DROP COLUMN IF EXISTS current_zone;
