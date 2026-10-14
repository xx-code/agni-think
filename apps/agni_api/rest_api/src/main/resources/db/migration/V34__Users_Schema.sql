-- =====================================================================================
-- V34 : authentication schema.
--
-- Introduit la table `users` (provisionnement via OAuth2/OIDC) et rattache chaque
-- `profiles` a un utilisateur. Migration idempotente.
-- =====================================================================================

-- -------------------------------------------------------------------------------------
-- 1. Utilisateurs
-- -------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Un utilisateur externe (claim `sub` de l'IdP) ne peut etre provisionne qu'une fois.
CREATE UNIQUE INDEX IF NOT EXISTS users_external_id_index ON users (external_id);

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS accounts_user_id_index ON accounts (user_id);

ALTER TABLE account_snapshot_balances ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS account_snapshot_balances_user_id_index ON account_snapshot_balances (user_id);

ALTER TABLE agent_suggestions ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS agent_suggestions_user_id_index ON agent_suggestions (user_id);

ALTER TABLE bank_registers ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS bank_registers_user_id_index ON bank_registers (user_id);

ALTER TABLE budgets ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS budgets_user_id_index ON budgets (user_id);

ALTER TABLE categories ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS categories_user_id_index ON categories (user_id);

ALTER TABLE currencies ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS currencies_user_id_index ON currencies (user_id);

ALTER TABLE deductions ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS deductions_user_id_index ON deductions (user_id);

ALTER TABLE external_transactions ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS external_transactions_user_id_index ON external_transactions (user_id);

ALTER TABLE finance_principles ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS finance_principles_user_id_index ON finance_principles (user_id);

ALTER TABLE finance_reports ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS finance_reports_user_id_index ON finance_reports (user_id);

ALTER TABLE funds ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS funds_user_id_index ON funds (user_id);

ALTER TABLE goals ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS goals_user_id_index ON goals (user_id);

ALTER TABLE income_sources ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS income_sources_user_id_index ON income_sources (user_id);

ALTER TABLE invoices ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS invoices_user_id_index ON invoices (user_id);

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS notifications_user_id_index ON notifications (user_id);

ALTER TABLE patrimonies ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS patrimonies_user_id_index ON patrimonies (user_id);

ALTER TABLE patrimony_snapshots ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS patrimony_snapshots_user_id_index ON patrimony_snapshots (user_id);

ALTER TABLE profiles ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS profiles_user_id_index ON profiles (user_id);

ALTER TABLE provisions ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS provisions_user_id_index ON provisions (user_id);

ALTER TABLE schedule_transactions ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS schedule_transactions_user_id_index ON schedule_transactions (user_id);

ALTER TABLE spending_periods ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS spending_periods_user_id_index ON spending_periods (user_id);

ALTER TABLE spending_period_templates ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS spending_period_templates_user_id_index ON spending_period_templates (user_id);

ALTER TABLE tags ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS tags_user_id_index ON tags (user_id);

ALTER TABLE transactions ADD COLUMN IF NOT EXISTS user_id UUID;
CREATE INDEX IF NOT EXISTS transactions_user_id_index ON transactions (user_id);