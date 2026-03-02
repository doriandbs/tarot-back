ALTER TABLE partie
    ADD COLUMN IF NOT EXISTS type_preneur VARCHAR(20),
    ADD COLUMN IF NOT EXISTS multiplicateur_preneur INT,
    ADD COLUMN IF NOT EXISTS points_contrat INT;
