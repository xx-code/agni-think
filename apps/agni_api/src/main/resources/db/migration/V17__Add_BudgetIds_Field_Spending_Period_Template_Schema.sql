ALTER TABLE spending_period_templates
    ADD COLUMN IF NOT EXISTS target_budget_ids JSONB DEFAULT '[]';
