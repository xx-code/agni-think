-- 1. Modification et ajout des colonnes existantes / nouvelles
ALTER TABLE provisions
ADD COLUMN IF NOT EXISTS is_patrimony BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS interest_loan DOUBLE PRECISION NOT NULL DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS loan_month BIGINT NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS floor_value DOUBLE PRECISION NOT NULL DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS type VARCHAR(255) NOT NULL DEFAULT 'DepreciateLoan',
ADD COLUMN IF NOT EXISTS depreciate_criteria jsonb NOT NULL DEFAULT '[]',
ADD COLUMN IF NOT EXISTS payment_info jsonb;

-- 2. Nettoyage de la colonne obsolète
ALTER TABLE provisions
DROP COLUMN IF EXISTS residual_value;