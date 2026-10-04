-- 1. Ajout des nouvelles colonnes (sans contrainte NOT NULL immédiate)
ALTER TABLE provisions
    ADD COLUMN IF NOT EXISTS cost_ht DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS cost_ttc DOUBLE PRECISION;

-- 2. Copie des données existantes depuis initial_cost
UPDATE provisions
SET
    cost_ht = initial_cost,
    cost_ttc = initial_cost
WHERE initial_cost IS NOT NULL;

-- 3. Application des contraintes et suppression de l'ancienne colonne
ALTER TABLE provisions
    ALTER COLUMN cost_ht SET NOT NULL,
ALTER COLUMN cost_ttc SET NOT NULL,
    DROP COLUMN IF EXISTS initial_cost;