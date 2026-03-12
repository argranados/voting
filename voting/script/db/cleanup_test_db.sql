-- cleanup_test_db.sql
-- Limpia datos de prueba para dejar la DB vacía

\c voting

BEGIN;

-- eliminar votos
TRUNCATE TABLE vote RESTART IDENTITY CASCADE;

-- eliminar nominaciones
TRUNCATE TABLE nomination RESTART IDENTITY CASCADE;

-- eliminar rondas
TRUNCATE TABLE round RESTART IDENTITY CASCADE;

-- eliminar contestants
TRUNCATE TABLE contestant RESTART IDENTITY CASCADE;

-- eliminar seasons
TRUNCATE TABLE season RESTART IDENTITY CASCADE;

COMMIT;

-- verificar que todo quedó limpio
SELECT 'season count', count(*) FROM season;
SELECT 'contestant count', count(*) FROM contestant;
SELECT 'round count', count(*) FROM round;
SELECT 'vote count', count(*) FROM vote;