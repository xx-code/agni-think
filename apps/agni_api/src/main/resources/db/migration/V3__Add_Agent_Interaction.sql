-- Table: agent_suggestions
CREATE TABLE agent_suggestions (
   agent_suggestion_id UUID PRIMARY KEY,
   agent_id VARCHAR(255) NOT NULL,
   agent_name VARCHAR(255) NOT NULL,
   title VARCHAR(255) NOT NULL,
   description TEXT,
   confidence_score DOUBLE PRECISION NOT NULL,
   status VARCHAR(50) NOT NULL
);

-- Table: bank_registers
CREATE TABLE bank_registers (
    bank_register_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    access_code VARCHAR(255) NOT NULL,
    cursor VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    accounts_linked TEXT -- Utilisé TEXT au cas où la liste est longue
);

-- Table: external_transactions
CREATE TABLE external_transactions (
   external_transaction_id UUID PRIMARY KEY,
   account_id VARCHAR(255) NOT NULL,
   amount DOUBLE PRECISION NOT NULL,
   merchant_name VARCHAR(255),
   category_primary VARCHAR(100),
   category_detail VARCHAR(100),
   is_treated BOOLEAN NOT NULL DEFAULT FALSE,
   date_transaction TIMESTAMP NOT NULL
);

-- Table: finance_reports
CREATE TABLE finance_reports (
    finance_report_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    date DATE NOT NULL
);