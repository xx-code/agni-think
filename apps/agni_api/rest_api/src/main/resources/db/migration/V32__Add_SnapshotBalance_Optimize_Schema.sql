-- =====================================================================================
-- V32 : migration IDEMPOTENTE (rejouable sur une base deja partiellement/totalement migree)
--
-- Principe : chaque operation verifie l'etat reel du schema courant (current_schema())
-- avant d'agir. Rien n'est ecrase, supprime ou renomme si le resultat existe deja.
-- =====================================================================================

-- -------------------------------------------------------------------------------------
-- 0. Fonctions utilitaires temporaires (disparaissent a la fin de la session)
-- -------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION pg_temp.has_table(t text) RETURNS boolean LANGUAGE plpgsql AS $fn$
BEGIN
RETURN EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = current_schema() AND table_name = t);
END $fn$;

CREATE OR REPLACE FUNCTION pg_temp.has_col(t text, c text) RETURNS boolean LANGUAGE plpgsql AS $fn$
BEGIN
RETURN EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = t AND column_name = c);
END $fn$;

-- Renomme une colonne seulement si l'ancienne existe ET que la nouvelle n'existe pas.
CREATE OR REPLACE FUNCTION pg_temp.rename_col(t text, old_c text, new_c text) RETURNS void LANGUAGE plpgsql AS $fn$
BEGIN
    IF pg_temp.has_col(t, old_c) AND NOT pg_temp.has_col(t, new_c) THEN
        EXECUTE format('ALTER TABLE %I RENAME COLUMN %I TO %I', t, old_c, new_c);
END IF;
END $fn$;

-- Renomme un index SUR UNE TABLE DONNEE, seulement si le nom cible est libre.
CREATE OR REPLACE FUNCTION pg_temp.rename_idx(t text, old_i text, new_i text) RETURNS void LANGUAGE plpgsql AS $fn$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_indexes
               WHERE schemaname = current_schema() AND tablename = t AND indexname = old_i)
       AND NOT EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
                       WHERE n.nspname = current_schema() AND c.relname = new_i) THEN
        EXECUTE format('ALTER INDEX %I RENAME TO %I', old_i, new_i);
END IF;
END $fn$;

-- NOT NULL : ignore si la colonne n'existe pas, est deja NOT NULL, ou contient des NULL (warning).
CREATE OR REPLACE FUNCTION pg_temp.set_not_null(t text, c text) RETURNS void LANGUAGE plpgsql AS $fn$
DECLARE nb bigint;
BEGIN
    IF NOT pg_temp.has_col(t, c) THEN
        RAISE WARNING 'set_not_null: colonne %.% inexistante, ignoree', t, c;
        RETURN;
END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = t
                 AND column_name = c AND is_nullable = 'NO') THEN
        RETURN;
END IF;
EXECUTE format('SELECT count(*) FROM %I WHERE %I IS NULL', t, c) INTO nb;
IF nb > 0 THEN
        RAISE WARNING 'set_not_null: %.% contient % valeur(s) NULL, contrainte NON appliquee', t, c, nb;
ELSE
        EXECUTE format('ALTER TABLE %I ALTER COLUMN %I SET NOT NULL', t, c);
END IF;
END $fn$;

-- CHECK : ajoute seulement si absente. NOT VALID => les lignes existantes ne sont pas
-- re-verifiees (aucun blocage), mais toutes les nouvelles ecritures le sont.
CREATE OR REPLACE FUNCTION pg_temp.add_check(t text, cname text, expr text) RETURNS void LANGUAGE plpgsql AS $fn$
BEGIN
    IF NOT pg_temp.has_table(t) THEN
        RAISE WARNING 'add_check: table % inexistante, % ignoree', t, cname;
        RETURN;
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint
                   WHERE conname = cname
                     AND conrelid = format('%I.%I', current_schema(), t)::regclass) THEN
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I CHECK (%s) NOT VALID', t, cname, expr);
END IF;
END $fn$;

-- =====================================================================================
-- 1. Renommage des tables / colonnes (ordre imperatif, chaque etape testee sur l'etat reel)
-- =====================================================================================

-- 1.1 + 1.2 Catalogue : `deduction_types` devient `deductions`.
--     L'ancienne table Knex `deductions` (colonne deduction_type_id) n'est supprimee que
--     si elle a encore l'ancienne structure ET que le catalogue `deduction_types` attend
--     de prendre sa place. Un `deductions` deja migre n'est JAMAIS touche.
DO $$
BEGIN
    IF pg_temp.has_table('deduction_types')
       AND pg_temp.has_table('deductions')
       AND pg_temp.has_col('deductions', 'deduction_type_id')
       AND NOT pg_temp.has_col('deductions', 'title') THEN
DROP TABLE deductions CASCADE;
END IF;

    IF pg_temp.has_table('deduction_types') AND NOT pg_temp.has_table('deductions') THEN
ALTER TABLE deduction_types RENAME TO deductions;
END IF;

    PERFORM pg_temp.rename_col('deductions', 'deduction_type_id', 'deduction_id');
    PERFORM pg_temp.rename_idx('deductions', 'deduction_types_pkey', 'deductions_pkey');
END $$;

-- 1.3 En-tete de facture : `transactions` devient `invoices`.
--     Un `transactions` n'est considere comme l'ANCIEN en-tete que s'il porte la colonne
--     `mouvement`/`movement` (la table de lignes n'en a pas). Cela evite de renommer par
--     erreur la nouvelle table de lignes (cause de l'erreur "invoice_id already exists").
DO $$
BEGIN
    IF NOT pg_temp.has_table('invoices')
       AND pg_temp.has_table('transactions')
       AND (pg_temp.has_col('transactions', 'mouvement') OR pg_temp.has_col('transactions', 'movement')) THEN
ALTER TABLE transactions RENAME TO invoices;
END IF;

    IF pg_temp.has_table('invoices') THEN
        PERFORM pg_temp.rename_col('invoices', 'transaction_id', 'invoice_id');
        PERFORM pg_temp.rename_col('invoices', 'mouvement', 'movement');

        -- Les index sont cibles PAR TABLE : on ne peut pas toucher ceux d'une autre table.
        PERFORM pg_temp.rename_idx('invoices', 'transactions_pkey', 'invoices_pkey');
        PERFORM pg_temp.rename_idx('invoices', 'transactions_account_id_index', 'invoices_account_id_index');
        PERFORM pg_temp.rename_idx('invoices', 'transactions_status_index', 'invoices_status_index');
END IF;

    IF pg_temp.has_table('internal_loans')
       AND EXISTS (SELECT 1 FROM pg_constraint
                   WHERE conname = 'fk_invoice'
                     AND conrelid = format('%I.%I', current_schema(), 'internal_loans')::regclass)
       AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_internal_loans_invoice') THEN
ALTER TABLE internal_loans RENAME CONSTRAINT fk_invoice TO fk_internal_loans_invoice;
END IF;
END $$;

-- Normalisation d'une ligne historique (idempotent : ne fait rien si deja 'Credit').
DO $$
BEGIN
    IF pg_temp.has_table('invoices') AND pg_temp.has_col('invoices', 'movement') THEN
UPDATE invoices SET movement = 'Credit' WHERE movement = 'credit';
END IF;
END $$;

-- 1.4 Lignes de facture : `records` devient `transactions` (seulement si `records` existe encore).
DO $$
BEGIN
    IF pg_temp.has_table('records') THEN
        PERFORM pg_temp.rename_col('records', 'transaction_id', 'invoice_id');

        IF NOT pg_temp.has_table('transactions') THEN
ALTER TABLE records RENAME TO transactions;
ELSE
            RAISE WARNING 'Table "transactions" deja presente : "records" non renommee (a verifier manuellement)';
END IF;
END IF;

    -- Uniquement sur la table de lignes (celle qui a invoice_id)
    IF pg_temp.has_table('transactions') AND pg_temp.has_col('transactions', 'invoice_id') THEN
        PERFORM pg_temp.rename_col('transactions', 'record_id', 'transaction_id');
        PERFORM pg_temp.rename_idx('transactions', 'records_pkey', 'transactions_pkey');
        PERFORM pg_temp.rename_idx('transactions', 'records_transaction_id_index', 'transactions_invoice_id_index');
END IF;
END $$;

-- =====================================================================================
-- 2. Snapshot de solde par compte.
-- =====================================================================================
CREATE TABLE IF NOT EXISTS account_snapshot_balances (
                                                         account_snapshot_balance_id UUID PRIMARY KEY,
                                                         account_id UUID NOT NULL,
                                                         balance DOUBLE PRECISION NOT NULL,
                                                         date TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT account_snapshot_balances_account_id_fkey
    FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS account_snapshot_balances_account_id_date_index
    ON account_snapshot_balances (account_id, date DESC);

-- =====================================================================================
-- 3. Contraintes de nullabilite (ignorees si deja posees, colonne absente ou NULL presents).
-- =====================================================================================
DO $$
DECLARE c text;
BEGIN
    FOREACH c IN ARRAY ARRAY['account_id','status','type','movement','date','is_freeze','deductions','invoice_module_linkers'] LOOP
        PERFORM pg_temp.set_not_null('invoices', c);
END LOOP;

    FOREACH c IN ARRAY ARRAY['invoice_id','money_amount','category_id','description','tag_ids','budget_ids'] LOOP
        PERFORM pg_temp.set_not_null('transactions', c);
END LOOP;

    FOREACH c IN ARRAY ARRAY['title','description','base','mode'] LOOP
        PERFORM pg_temp.set_not_null('deductions', c);
END LOOP;
END $$;

-- =====================================================================================
-- 4. Domaines de valeurs (CHECK ajoutes seulement s'ils n'existent pas, sans bloquer
--    sur les donnees historiques grace a NOT VALID).
-- =====================================================================================
DO $$
BEGIN
    PERFORM pg_temp.add_check('invoices', 'invoices_movement_check', $c$movement IN ('Credit', 'Debit')$c$);
    PERFORM pg_temp.add_check('invoices', 'invoices_status_check',   $c$status IN ('Pending', 'Complete')$c$);
    PERFORM pg_temp.add_check('invoices', 'invoices_type_check',     $c$type IN ('Income', 'FixedCost', 'VariableCost', 'Other')$c$);
    PERFORM pg_temp.add_check('deductions', 'deductions_base_check', $c$base IN ('Subtotal', 'Total')$c$);
    PERFORM pg_temp.add_check('deductions', 'deductions_mode_check', $c$mode IN ('Flat', 'Rate')$c$);
    PERFORM pg_temp.add_check('transactions', 'transactions_money_amount_check', $c$money_amount > 0$c$);
    PERFORM pg_temp.add_check('accounts', 'accounts_type_check',
        $c$type IN ('Checking', 'CreditCard', 'Saving', 'Business', 'Broking')$c$);
    PERFORM pg_temp.add_check('funds', 'funds_type_check',
        $c$type IN ('Emergency', 'Amortization', 'SinkingFund', 'ProjectTarget', 'Opportunity', 'SavingsGeneral')$c$);
    PERFORM pg_temp.add_check('spending_periods', 'spending_periods_state_check',
        $c$state IN ('Draft', 'ToReview', 'InProgress', 'Complete')$c$);
END $$;

-- =====================================================================================
-- 5. Index manquants & nettoyage (tous gardes).
-- =====================================================================================
DO $$
BEGIN
    IF pg_temp.has_table('invoices') THEN
CREATE INDEX IF NOT EXISTS invoices_account_id_date_index ON invoices (account_id, date DESC);
DROP INDEX IF EXISTS invoices_account_id_index;
END IF;

    IF pg_temp.has_table('transactions') AND pg_temp.has_col('transactions', 'category_id') THEN
CREATE INDEX IF NOT EXISTS transactions_category_id_index ON transactions (category_id);
END IF;

    IF pg_temp.has_table('internal_loans') AND pg_temp.has_col('internal_loans', 'invoice_id') THEN
CREATE INDEX IF NOT EXISTS internal_loans_invoice_id_index ON internal_loans (invoice_id);
END IF;

    IF pg_temp.has_table('spending_periods') AND pg_temp.has_col('spending_periods', 'spending_period_template_id') THEN
CREATE INDEX IF NOT EXISTS spending_periods_spending_period_template_id_index
    ON spending_periods (spending_period_template_id);
END IF;

    -- Renommage herite de V7
    PERFORM pg_temp.rename_idx('funds', 'save_goals_pkey', 'funds_pkey');

    IF pg_temp.has_table('funds') AND pg_temp.has_col('funds', 'account_id') THEN
CREATE INDEX IF NOT EXISTS funds_account_id_index ON funds (account_id);
END IF;
    IF pg_temp.has_table('holdings') AND pg_temp.has_col('holdings', 'account_id') THEN
CREATE INDEX IF NOT EXISTS holdings_account_id_index ON holdings (account_id);
END IF;
    IF pg_temp.has_table('income_sources') AND pg_temp.has_col('income_sources', 'linked_account_id') THEN
CREATE INDEX IF NOT EXISTS income_sources_linked_account_id_index ON income_sources (linked_account_id);
END IF;
    IF pg_temp.has_table('provisions') AND pg_temp.has_col('provisions', 'fund_amortization_id') THEN
CREATE INDEX IF NOT EXISTS provisions_fund_amortization_id_index ON provisions (fund_amortization_id);
END IF;
END $$;