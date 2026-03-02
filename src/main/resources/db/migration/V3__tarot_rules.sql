ALTER TABLE partie
    ADD COLUMN IF NOT EXISTS bouts_gagnants INT,
    ADD COLUMN IF NOT EXISTS points_faits_gagnants INT,
    ADD COLUMN IF NOT EXISTS points_a_faire INT,
    ADD COLUMN IF NOT EXISTS points_de_base INT;

ALTER TABLE score_partie
    ADD COLUMN IF NOT EXISTS gagnant BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS bonus_partie (
    id UUID PRIMARY KEY,
    partie_id UUID NOT NULL REFERENCES partie(id) ON DELETE CASCADE,
    session_participant_id UUID NOT NULL REFERENCES session_participant(id) ON DELETE CASCADE,
    type_bonus VARCHAR(30) NOT NULL,
    points INT NOT NULL DEFAULT 40
);

CREATE INDEX IF NOT EXISTS idx_bonus_partie_partie_id ON bonus_partie(partie_id);
CREATE INDEX IF NOT EXISTS idx_bonus_partie_session_participant_id ON bonus_partie(session_participant_id);
