-- Migration pour les provisions (Objets à amortir/épargner)
CREATE TABLE provisions (
    provision_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    initial_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    acquisition_date DATE NOT NULL,
    expected_lifespan_month INTEGER NOT NULL DEFAULT 0,
    residual_value DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    -- Champs hérités de JdbcModel (souvent created_at/updated_at)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Migration pour les sources de revenus (Income Streams)
CREATE TABLE income_sources (
    income_source_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- FIXED, VARIABLE, CONTRACT
    pay_frequency VARCHAR(50) NOT NULL, -- WEEKLY, BIWEEKLY, etc.
    reliability_level INTEGER NOT NULL DEFAULT 5, -- 1 à 10
    start_date DATE NOT NULL,
    end_date DATE, -- Peut être null pour un CDI
    tax_rate DOUBLE PRECISION DEFAULT 0.0,
    other_rate DOUBLE PRECISION DEFAULT 0.0,
    linked_account_id UUID, -- Foreign Key vers tes comptes
    annual_gross_amount DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Migration pour les principes financiers (La "philosophie" de l'agent)
CREATE TABLE finance_principles (
    finance_principle_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    strictness INTEGER NOT NULL DEFAULT 1, -- 1 à 10
    logic_rules TEXT, -- Stockage de JSON ou texte brut pour l'IA
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index pour optimiser les recherches fréquentes
CREATE INDEX idx_income_sources_type ON income_sources(type);
CREATE INDEX idx_provisions_acquisition ON provisions(acquisition_date);