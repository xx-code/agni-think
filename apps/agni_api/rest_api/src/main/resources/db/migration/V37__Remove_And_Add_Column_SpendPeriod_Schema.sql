ALTER TABLE spending_periods
DROP COLUMN IF EXISTS free_amount;

ALTER TABLE spending_periods
DROP COLUMN IF EXISTS want_spending_items;

ALTER TABLE spending_periods
DROP COLUMN IF EXISTS total_expected_income;

ALTER TABLE spending_periods
DROP COLUMN IF EXISTS total_expected_expenses;

ALTER TABLE spending_periods
ADD COLUMN IF NOT EXISTS schedule_incomes_selected JSONB DEFAULT '[]';

ALTER TABLE spending_periods
ADD COLUMN IF NOT EXISTS schedule_expenses_selected JSONB DEFAULT '[]';

ALTER TABLE spending_periods
ADD COLUMN IF NOT EXISTS budgets_selected JSONB DEFAULT '[]';

ALTER TABLE spending_periods
ADD COLUMN IF NOT EXISTS wishlist_item_selected JSONB DEFAULT '[]';