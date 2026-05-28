-- MySQL 8: выполните в MySQL Workbench или: mysql -u root -p < scripts/init-database-mysql.sql
-- Совпадает с application.yml (user hike, database hike_load)

CREATE DATABASE IF NOT EXISTS hike_load
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'hike'@'localhost' IDENTIFIED BY 'hike_secret';
GRANT ALL PRIVILEGES ON hike_load.* TO 'hike'@'localhost';
FLUSH PRIVILEGES;
