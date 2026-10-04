-- 1. Renommer la table 'save_goals' en 'funds'
ALTER TABLE save_goals
RENAME TO funds;

-- 2. Renommer la clé primaire/colonne id
ALTER TABLE funds
RENAME COLUMN save_goal_id TO fund_id;

-- 3. Supprimer les colonnes retirées du modèle
ALTER TABLE funds
DROP COLUMN IF EXISTS desir_value,
DROP COLUMN IF EXISTS importance,
DROP COLUMN IF EXISTS wish_due_date,
DROP COLUMN IF EXISTS items;

ALTER TABLE budgets
DROP COLUMN IF EXISTS save_goal_ids;
