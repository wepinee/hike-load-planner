-- Выполните в MySQL Workbench, если раскладка падает с "gear_item_id cannot be null"
USE hike_load;

ALTER TABLE assignments MODIFY COLUMN gear_item_id BIGINT NULL;
ALTER TABLE assignments MODIFY COLUMN food_item_id BIGINT NULL;
