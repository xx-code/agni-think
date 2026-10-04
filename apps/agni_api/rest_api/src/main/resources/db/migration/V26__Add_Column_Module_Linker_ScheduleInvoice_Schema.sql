ALTER TABLE schedule_transactions
    ADD COLUMN IF NOT EXISTS module_linker JSONB DEFAULT NULL;
