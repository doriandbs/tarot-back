CREATE TABLE IF NOT EXISTS utilisateur (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    cree_le TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS joueur (
    id UUID PRIMARY KEY,
    utilisateur_id UUID NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    nom VARCHAR(120) NOT NULL,
    cree_le TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_joueur_utilisateur_nom UNIQUE (utilisateur_id, nom)
);

CREATE INDEX IF NOT EXISTS idx_joueur_utilisateur_id ON joueur(utilisateur_id);

CREATE TABLE IF NOT EXISTS session_tarot (
    id UUID PRIMARY KEY,
    utilisateur_id UUID NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    nom VARCHAR(150),
    date_session DATE NOT NULL,
    cree_le TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_session_tarot_utilisateur_id ON session_tarot(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_session_tarot_date_session ON session_tarot(date_session);

CREATE TABLE IF NOT EXISTS session_participant (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES session_tarot(id) ON DELETE CASCADE,
    joueur_id UUID NOT NULL REFERENCES joueur(id) ON DELETE RESTRICT,
    cree_le TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_session_participant_session_joueur UNIQUE (session_id, joueur_id)
);

CREATE INDEX IF NOT EXISTS idx_session_participant_session_id ON session_participant(session_id);
CREATE INDEX IF NOT EXISTS idx_session_participant_joueur_id ON session_participant(joueur_id);

CREATE TABLE IF NOT EXISTS partie (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES session_tarot(id) ON DELETE CASCADE,
    numero INT NOT NULL,
    joue_le TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_partie_session_numero UNIQUE (session_id, numero)
);

CREATE INDEX IF NOT EXISTS idx_partie_session_id ON partie(session_id);

CREATE TABLE IF NOT EXISTS score_partie (
    id UUID PRIMARY KEY,
    partie_id UUID NOT NULL REFERENCES partie(id) ON DELETE CASCADE,
    session_participant_id UUID NOT NULL REFERENCES session_participant(id) ON DELETE CASCADE,
    points INT NOT NULL,
    CONSTRAINT uq_score_partie_participant UNIQUE (partie_id, session_participant_id)
);

CREATE INDEX IF NOT EXISTS idx_score_partie_partie_id ON score_partie(partie_id);
CREATE INDEX IF NOT EXISTS idx_score_partie_session_participant_id ON score_partie(session_participant_id);
