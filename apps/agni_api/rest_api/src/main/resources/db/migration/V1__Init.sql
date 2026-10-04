CREATE TABLE IF NOT EXISTS currencies (
    "currency_id" uuid PRIMARY KEY,
    "name" varchar(255),
    "symbol" varchar(10),
    "locale" varchar(20),
    "rate_to_base" numeric(19,6) DEFAULT 1,
    "is_base" boolean DEFAULT false
);

CREATE TABLE IF NOT EXISTS categories (
    "category_id" uuid PRIMARY KEY,
    "title" varchar(255) NOT NULL,
    "color" varchar(50) NOT NULL,
    "icon_id" varchar(100),
    "is_system" boolean DEFAULT false
);

CREATE TABLE IF NOT EXISTS tags (
    "tag_id" uuid PRIMARY KEY,
    "value" varchar(255) NOT NULL,
    "color" varchar(50),
    "is_system" boolean DEFAULT false
);
CREATE UNIQUE INDEX IF NOT EXISTS tags_value_unique ON tags(value);

CREATE TABLE IF NOT EXISTS deduction_types (
    "deduction_type_id" uuid PRIMARY KEY,
    "title" varchar(255),
    "description" text,
    "base" varchar(255),
    "mode" varchar(255)
);

CREATE TABLE IF NOT EXISTS accounts (
    "account_id" uuid PRIMARY KEY,
    "title" varchar(255),
    "type" varchar(50),
    "balance" numeric(19,4) DEFAULT 0,
    "currency_id" uuid REFERENCES currencies(currency_id) ON DELETE SET NULL,
    "detail" jsonb DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS budgets (
    "budget_id" uuid PRIMARY KEY,
    "title" varchar(255),
    "target" numeric(19,4) DEFAULT 0,
    "scheduler" jsonb,
    "is_archived" boolean DEFAULT false,
    "save_goal_ids" jsonb DEFAULT '[]'
);

CREATE TABLE IF NOT EXISTS save_goals (
    "save_goal_id" uuid PRIMARY KEY,
    "title" varchar(255),
    "target" numeric(19,4) DEFAULT 0,
    "balance" numeric(19,4) DEFAULT 0,
    "desir_value" integer,
    "importance" integer,
    "wish_due_date" date,
    "description" text,
    "account_id" uuid REFERENCES accounts(account_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    "transaction_id" uuid PRIMARY KEY,
    "account_id" uuid REFERENCES accounts(account_id) ON DELETE CASCADE,
    "status" varchar(50),
    "type" varchar(50),
    "date" timestamptz DEFAULT now(),
    "is_freeze" boolean DEFAULT false,
    "deductions" jsonb DEFAULT '[]',
    "mouvement" varchar(50)
);

CREATE TABLE IF NOT EXISTS records (
    "record_id" uuid PRIMARY KEY,
    "money_amount" numeric(19,4) DEFAULT 0,
    "description" text,
    "transaction_id" uuid,
    "category_id" uuid REFERENCES categories(category_id) ON DELETE SET NULL,
    "tag_ids" jsonb DEFAULT '[]',
    "budget_ids" jsonb DEFAULT '[]'
);

CREATE TABLE IF NOT EXISTS schedule_transactions (
    "schedule_transaction_id" uuid PRIMARY KEY,
    "account_id" uuid REFERENCES accounts(account_id) ON DELETE CASCADE,
    "category_id" uuid REFERENCES categories(category_id) ON DELETE SET NULL,
    "amount" numeric(19,4) DEFAULT 0,
    "name" varchar(255),
    "type" varchar(50),
    "is_pause" boolean DEFAULT false,
    "is_freeze" boolean DEFAULT false,
    "scheduler" jsonb,
    "tag_ids" jsonb DEFAULT '[]'
);

CREATE TABLE IF NOT EXISTS patrimonies (
    "patrimony_id" uuid PRIMARY KEY,
    "title" varchar(255),
    "amount" numeric(19,4) DEFAULT 0,
    "type" varchar(50),
    "account_ids" jsonb DEFAULT '[]',
    "created_at" date DEFAULT CURRENT_DATE,
    "updated_at" date DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS patrimony_snapshots (
    "patrimony_snapshot_id" uuid PRIMARY KEY,
    "patrimony_id" uuid REFERENCES patrimonies(patrimony_id) ON DELETE CASCADE,
    "balance" numeric(19,4) DEFAULT 0,
    "date" date DEFAULT CURRENT_DATE,
    "status" varchar(50)
);

CREATE TABLE IF NOT EXISTS notifications (
    "notification_id" uuid PRIMARY KEY,
    "title" varchar(255),
    "content" text,
    "is_read" boolean DEFAULT false,
    "date" timestamptz DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_date ON transactions(account_id, date);
CREATE INDEX IF NOT EXISTS idx_records_transaction ON records(transaction_id);
CREATE INDEX IF NOT EXISTS idx_records_category ON records(category_id);
CREATE INDEX IF NOT EXISTS idx_patrimony_snapshots_patrimony ON patrimony_snapshots(patrimony_id);
