ALTER TABLE provisions
    ADD COLUMN IF NOT EXISTS is_installment_on_ttc boolean DEFAULT true;