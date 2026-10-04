CREATE TABLE IF NOT EXISTS goals (
    goal_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    source_id UUID NOT NULL,
    description TEXT NOT NULL,
    due_date DATE NOT NULL,
    target_amount DOUBLE PRECISION NOT NULL,
    status INT NOT NULL,
    type VARCHAR(255) NOT NULL
);