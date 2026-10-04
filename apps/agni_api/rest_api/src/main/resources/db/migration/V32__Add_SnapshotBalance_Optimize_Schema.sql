-- =====================================================================================
-- 1. Renommage des tables / colonnes pour coller aux modeles Kotlin (`@Table`,
--    `getTableName()`, `getEntityModelFieldName()`).
--
--    L'ordre des instructions est imperative : chaque renommage libere un nom qui est
--    reutilise juste apres. Separer ces operations en plusieurs migrations echouerait sur
--    `relation "..." already exists`.
-- =====================================================================================

-- 1.1 `deductions` (table Knex : deduction_id, deduction_type_id, rate) n'est lue par
--     aucun code Kotlin : les deductions d'une facture vivent dans
--     `invoices.deductions` (jsonb). Table vide, on la supprime pour liberer le nom
--     `deductions`, qui doit revenir au catalogue `deduction_types`.
DROP TABLE IF EXISTS deductions;

-- 1.2 Catalogue des deductions : `deduction_types` devient `deductions`.
--     `ALTER TABLE ... RENAME TO` ne renomme ni la cle primaire ni ses index : le nom
--     `deduction_types_pkey` doit etre libere explicitement avant d'etre reutilise.
ALTER TABLE deduction_types RENAME TO deductions;
ALTER TABLE deductions RENAME COLUMN deduction_type_id TO deduction_id;
ALTER INDEX deduction_types_pkey RENAME TO deductions_pkey;

-- 1.3 En-tete de facture : `transactions` devient `invoices`.
--     La colonne `mouvement` (faute de frappe historique) devient `movement` :
--     `JdbcInvoiceModel.movement` est lu par `SELECT *` via `DataClassRowMapper`.
ALTER TABLE transactions RENAME COLUMN transaction_id TO invoice_id;
ALTER TABLE transactions RENAME COLUMN mouvement TO movement;
ALTER TABLE transactions RENAME TO invoices;

-- Une ligne historique stockait 'credit' au lieu de la valeur de l'enum
-- `InvoiceMovementType.CREDIT` : les agrégats de solde filtrent sur 'Credit'.
UPDATE invoices SET movement = 'Credit' WHERE movement = 'credit';

ALTER INDEX transactions_pkey RENAME TO invoices_pkey;
ALTER INDEX transactions_account_id_index RENAME TO invoices_account_id_index;
ALTER INDEX transactions_status_index RENAME TO invoices_status_index;

-- La cle etrangere de `internal_loans` a suivi la table automatiquement, mais garde son
-- ancien nom : on lui redonne un nom coherent.
ALTER TABLE internal_loans RENAME CONSTRAINT fk_invoice TO fk_internal_loans_invoice;

-- 1.4 Lignes de facture : `records` devient `transactions`.
--     Renommer d'abord `transaction_id` -> `invoice_id` (la colonne pointe vers
--     `invoices`), sinon `record_id` ne pourra pas prendre le nom `transaction_id`
--     deja occupe sur la meme table.
ALTER TABLE records RENAME COLUMN transaction_id TO invoice_id;
ALTER TABLE records RENAME TO transactions;
ALTER TABLE transactions RENAME COLUMN record_id TO transaction_id;

ALTER INDEX records_pkey RENAME TO transactions_pkey;
ALTER INDEX records_transaction_id_index RENAME TO transactions_invoice_id_index;

-- =====================================================================================
-- 2. Snapshot de solde par compte.
--    `date` est NOT NULL : `JdbcAccountSnapshotBalance.date` et le RowMapper de
--    `AccountSnapshotBalanceRepository` la lisent sans valeur par defaut.
-- =====================================================================================
CREATE TABLE IF NOT EXISTS account_snapshot_balances (
    account_snapshot_balance_id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    balance DOUBLE PRECISION NOT NULL,
    date TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT account_snapshot_balances_account_id_fkey
        FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);

-- Requete de l'historique : `WHERE account_id = ? AND date <= ? ORDER BY date DESC`.
CREATE INDEX IF NOT EXISTS account_snapshot_balances_account_id_date_index
    ON account_snapshot_balances (account_id, date DESC);

-- =====================================================================================
-- 3. Contraintes de nullabilite alignees sur les modeles Kotlin.
--    Toutes ces colonnes sont non nulles dans les donnees existantes et declarees non
--    nullables dans les data classes `JdbcInvoiceModel`, `JdbcTransactionModel` et
--    `JdbcDeductionModel` : une valeur nulle ferait echouer la lecture par
--    `DataClassRowMapper` avant meme d'atteindre la couche metier.
-- =====================================================================================
ALTER TABLE invoices
    ALTER COLUMN account_id SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN type SET NOT NULL,
    ALTER COLUMN movement SET NOT NULL,
    ALTER COLUMN date SET NOT NULL,
    ALTER COLUMN is_freeze SET NOT NULL,
    ALTER COLUMN deductions SET NOT NULL,
    ALTER COLUMN invoice_module_linkers SET NOT NULL;

ALTER TABLE transactions
    ALTER COLUMN invoice_id SET NOT NULL,
    ALTER COLUMN money_amount SET NOT NULL,
    ALTER COLUMN category_id SET NOT NULL,
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN tag_ids SET NOT NULL,
    ALTER COLUMN budget_ids SET NOT NULL;

ALTER TABLE deductions
    ALTER COLUMN title SET NOT NULL,
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN base SET NOT NULL,
    ALTER COLUMN mode SET NOT NULL;

-- =====================================================================================
-- 4. Domaines de valeurs.
--    Les listes reprennent exactement les enums Kotlin (`domain.enums`), qui sont
--    volontairement en base aujourd'hui. Elles protgent les agregats de solde et de
--    depense des valeurs parasites ('credit' a ete tolere par le VARCHAR).
-- =====================================================================================
ALTER TABLE invoices
    ADD CONSTRAINT invoices_movement_check CHECK (movement IN ('Credit', 'Debit')),
    ADD CONSTRAINT invoices_status_check CHECK (status IN ('Pending', 'Complete')),
    ADD CONSTRAINT invoices_type_check CHECK (type IN ('Income', 'FixedCost', 'VariableCost', 'Other'));

ALTER TABLE deductions
    ADD CONSTRAINT deductions_base_check CHECK (base IN ('Subtotal', 'Total')),
    ADD CONSTRAINT deductions_mode_check CHECK (mode IN ('Flat', 'Rate'));

ALTER TABLE transactions
    ADD CONSTRAINT transactions_money_amount_check CHECK (money_amount > 0);

ALTER TABLE accounts
    ADD CONSTRAINT accounts_type_check CHECK (type IN ('Checking', 'CreditCard', 'Saving', 'Business', 'Broking'));

ALTER TABLE funds
    ADD CONSTRAINT funds_type_check CHECK (type IN ('Emergency', 'Amortization', 'SinkingFund',
                                                  'ProjectTarget', 'Opportunity', 'SavingsGeneral'));

ALTER TABLE spending_periods
    ADD CONSTRAINT spending_periods_state_check CHECK (state IN ('Draft', 'ToReview', 'InProgress', 'Complete'));

-- =====================================================================================
-- 5. Index manquants.
--    Les agregats (`GetBalancesByPeriod`, tableau de bord) filtrent par compte puis
--    parcourent les dates ; les lignes de facture sont toujours lues via leur facture.
--    Aucun index n'existait sur `date`, et `invoices_account_id_index` est Nowredundant
--    avec le composite `(account_id, date DESC)`.
-- =====================================================================================
CREATE INDEX IF NOT EXISTS invoices_account_id_date_index ON invoices (account_id, date DESC);
DROP INDEX IF EXISTS invoices_account_id_index;

CREATE INDEX IF NOT EXISTS transactions_category_id_index ON transactions (category_id);
CREATE INDEX IF NOT EXISTS internal_loans_invoice_id_index ON internal_loans (invoice_id);
CREATE INDEX IF NOT EXISTS spending_periods_spending_period_template_id_index
    ON spending_periods (spending_period_template_id);

-- Renommage heritage de V7 (la table s'appelait alors `save_goals`).
ALTER INDEX save_goals_pkey RENAME TO funds_pkey;
CREATE INDEX IF NOT EXISTS funds_account_id_index ON funds (account_id);
CREATE INDEX IF NOT EXISTS holdings_account_id_index ON holdings (account_id);
CREATE INDEX IF NOT EXISTS income_sources_linked_account_id_index ON income_sources (linked_account_id);
CREATE INDEX IF NOT EXISTS provisions_fund_amortization_id_index ON provisions (fund_amortization_id);