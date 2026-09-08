CREATE TABLE IF NOT EXISTS spending_periods (
    spending_period_id UUID PRIMARY KEY,
    spending_period_template_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    suggestion_amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    savings_target DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_expected_income DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_expected_expenses DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    state VARCHAR(255) NOT NULL,
    want_spending_items JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_spending_period_template
    FOREIGN KEY (spending_period_template_id)
    REFERENCES spending_period_templates (spending_period_template_id)
    ON DELETE CASCADE
);