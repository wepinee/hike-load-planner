package ru.hikeload.service;

/**
 * Базовое исключение прикладного слоя (иерархия для бизнес-ошибок).
 */
public abstract class AppException extends RuntimeException {

    protected AppException(String message) {
        super(message);
    }
}
