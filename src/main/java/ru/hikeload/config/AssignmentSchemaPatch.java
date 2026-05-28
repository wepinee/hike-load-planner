package ru.hikeload.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Обновляет таблицу assignments: gear_item_id и food_item_id допускают NULL
 * (еда без снаряжения, снаряжение без еды).
 */
@Component
public class AssignmentSchemaPatch implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AssignmentSchemaPatch.class);

    private final JdbcTemplate jdbcTemplate;

    public AssignmentSchemaPatch(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE assignments MODIFY COLUMN gear_item_id BIGINT NULL");
            jdbcTemplate.execute("ALTER TABLE assignments MODIFY COLUMN food_item_id BIGINT NULL");
            log.info("Таблица assignments: gear_item_id и food_item_id разрешены как NULL");
        } catch (Exception e) {
            log.debug("Патч схемы assignments не потребовался или уже применён: {}", e.getMessage());
        }
    }
}
