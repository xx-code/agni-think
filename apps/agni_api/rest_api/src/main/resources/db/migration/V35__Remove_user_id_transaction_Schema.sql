DROP INDEX IF EXISTS transactions_user_id_index;
ALTER TABLE transactions
DROP COLUMN IF EXISTS user_id;