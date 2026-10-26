ALTER TABLE bank_registers
ALTER COLUMN accounts_linked TYPE JSONB USING accounts_linked::jsonb;