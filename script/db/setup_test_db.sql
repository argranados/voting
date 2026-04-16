-- Conectarse a la base de datos voting_db
\c voting;

-- Insertar datos de prueba
INSERT INTO season (id, name, status, created_at)
VALUES (1, 'Season 1', 'ACTIVE', now());

INSERT INTO contestant (id, season_id, name, status, created_at)
VALUES
    (1, 1, 'Contestant A', 'ACTIVE', now()),
    (2, 1, 'Contestant B', 'ACTIVE', now());

-- Validar datos insertados
SELECT * FROM season;
SELECT * FROM contestant;
