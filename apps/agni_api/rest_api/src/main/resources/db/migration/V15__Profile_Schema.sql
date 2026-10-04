-- 1. Création de la table profiles
CREATE TABLE IF NOT EXISTS profiles (
    profile_id UUID PRIMARY KEY,
    max_wishlist_amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    fix_spend_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    varial_spend_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    saving_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Insertion d'un seul élément initialisé à zéro
INSERT INTO profiles (
    profile_id,
    max_wishlist_amount,
    fix_spend_percentage,
    varial_spend_percentage,
    saving_percentage
)
VALUES (
   '457ae73e-8124-4d3b-ab2b-d6a404c6b4d3',
   0.0,
   0.0,
   0.0,
   0.0
)
ON CONFLICT (profile_id) DO NOTHING;