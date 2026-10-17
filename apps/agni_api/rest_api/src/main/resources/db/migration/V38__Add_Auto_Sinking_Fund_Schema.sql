-- Étape 1 : Ajout de la nouvelle colonne invoice_type
ALTER TABLE schedule_transactions
    ADD COLUMN IF NOT EXISTS invoice_type VARCHAR(50);

-- Étape 2 : Transfert des anciennes valeurs de type vers invoice_type
UPDATE schedule_transactions
SET invoice_type = type
WHERE invoice_type IS NULL;

-- Étape 3 : Mise à jour de la colonne type en fonction de is_freeze
UPDATE schedule_transactions
SET type = CASE
   WHEN is_freeze = TRUE THEN 'Freeze'
   ELSE 'Standard'
END;

-- Étape 4 : Suppression de la colonne is_freeze
ALTER TABLE schedule_transactions
DROP COLUMN IF EXISTS is_freeze;

-- Ajout de la colonne scheduler de type JSONB
ALTER TABLE funds
    ADD COLUMN IF NOT EXISTS sinking_fund_detail JSONB;