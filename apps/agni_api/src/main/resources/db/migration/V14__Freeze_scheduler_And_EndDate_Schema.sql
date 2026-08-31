-- 1. Ajout des nouvelles colonnes
ALTER TABLE schedule_transactions
    ADD COLUMN IF NOT EXISTS end_date TIMESTAMPTZ DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS freeze_scheduler JSONB DEFAULT NULL;

-- 2. Copie de la colonne scheduler vers freeze_scheduler pour les transactions dont is_freeze est vrai
UPDATE schedule_transactions
SET freeze_scheduler = scheduler
WHERE is_freeze = TRUE;