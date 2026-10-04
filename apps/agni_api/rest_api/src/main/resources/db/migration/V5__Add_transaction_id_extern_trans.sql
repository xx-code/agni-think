ALTER TABLE external_transactions
ADD COLUMN transaction_id VARCHAR(255) DEFAULT gen_random_uuid()::text;