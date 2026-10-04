ALTER TABLE spending_periods
    RENAME COLUMN suggestion_amount TO free_amount;

ALTER TABLE spending_periods
    ADD COLUMN IF NOT EXISTS close_balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS forcast_snapshot TEXT NOT NULL DEFAULT '';
