ALTER TABLE partie
    ALTER COLUMN multiplicateur_preneur TYPE NUMERIC(6,2) USING multiplicateur_preneur::numeric,
    ALTER COLUMN points_contrat TYPE NUMERIC(10,2) USING points_contrat::numeric;

ALTER TABLE score_partie
    ALTER COLUMN points TYPE NUMERIC(10,2) USING points::numeric;
