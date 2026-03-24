CREATE TABLE internal_loan (
    internal_loan_id UUID PRIMARY KEY,
    credit_target_id UUID NOT NULL,
    invoice_id UUID NOT NULL,
    fund_source_id UUID NOT NULL,
    due_date DATE NOT NULL,
    CONSTRAINT fk_credit_target FOREIGN KEY (credit_target_id) REFERENCES accounts(id),
    CONSTRAINT fk_fund_source FOREIGN KEY (fund_source_id) REFERENCES accounts(id),
    CONSTRAINT fk_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

-- Indexation pour optimiser les recherches par compte (très probable dans ton app)
CREATE INDEX idx_internal_loan_fund_source ON internal_loan(fund_source_id);
CREATE INDEX idx_internal_loan_credit_target ON internal_loan(credit_target_id);