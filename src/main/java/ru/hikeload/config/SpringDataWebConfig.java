package ru.hikeload.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Стабильный JSON для GET /api/hikes?page=0 (content + page metadata).
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class SpringDataWebConfig {
}
