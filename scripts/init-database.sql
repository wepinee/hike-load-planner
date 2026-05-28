-- УСТАРЕЛО: проект переведён на MySQL. Используйте scripts/init-database-mysql.sql
--
-- PostgreSQL (если вернётесь на PG):
-- Выполните в pgAdmin или psql от имени postgres.

CREATE USER hike WITH PASSWORD 'hike_secret';

CREATE DATABASE hike_load
    OWNER hike
    ENCODING 'UTF8';

GRANT ALL PRIVILEGES ON DATABASE hike_load TO hike;
