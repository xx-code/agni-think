ALTER TABLE provisions
    ADD COLUMN IF NOT EXISTS fund_amortization_Id uuid DEFAULT NULL;
