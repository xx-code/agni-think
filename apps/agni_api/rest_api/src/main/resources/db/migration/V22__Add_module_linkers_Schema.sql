ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS invoice_module_linkers jsonb DEFAULT '[]';
