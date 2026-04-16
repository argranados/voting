-- cleanup_test_db.sql
-- Limpia datos de prueba para dejar la DB vacía

\c voting

BEGIN;

-- eliminar datos
TRUNCATE TABLE vote, nomination, round, contestant, season
RESTART IDENTITY CASCADE;

COMMIT;

-- verificar que todo quedó limpio
SELECT 'season count', count(*) FROM season;
SELECT 'contestant count', count(*) FROM contestant;
SELECT 'round count', count(*) FROM round;
SELECT 'vote count', count(*) FROM vote;