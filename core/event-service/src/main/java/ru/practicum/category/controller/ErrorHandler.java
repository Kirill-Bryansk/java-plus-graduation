package ru.practicum.category.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.errors.ApiError;
import ru.practicum.errors.exceptions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Глобальный обработчик исключений для модуля категорий.
 * Перехватывает бизнес-исключения и преобразует их в стандартный ApiError.
 *
 * NotFoundException  → 404
 * ValidationException → 400
 * DataAlreadyInUseException → 409
 * ForbiddenException → 403
 * ConditionsNotMetException → 409
 * MethodArgumentNotValidException → 400
 * MissingServletRequestParameterException → 400
 * Throwable (fallback) → 500
 */
@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${api.log.stacktrace}")
    private boolean isStackTrace;

    /**
     * Обработка NotFoundException (объект не найден) → 404.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error("404 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = isStackTrace ? sw.toString() : "Stack trace not allowed by property";
        return new ApiError(stackTrace, e.getMessage(), "The required object was not found.",
                HttpStatus.NOT_FOUND, LocalDateTime.now().format(formatter));
    }

    /**
     * Обработка ValidationException (некорректные данные) → 400.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.error("400 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = isStackTrace ? sw.toString() : "Stack trace not allowed by property";
        return new ApiError(stackTrace, e.getMessage(), "Incorrectly made request.",
                HttpStatus.BAD_REQUEST, LocalDateTime.now().format(formatter));
    }

    /**
     * Обработка DataAlreadyInUseException (нарушение уникальности) → 409.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataAlreadyInUseException(final DataAlreadyInUseException e) {
        log.error("409 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = isStackTrace ? sw.toString() : "Stack trace not allowed by property";
        return new ApiError(stackTrace, e.getMessage(), "Integrity constraint has been violated.",
                HttpStatus.CONFLICT, LocalDateTime.now().format(formatter));
    }

    /**
     * Обработка ошибок валидации @Valid (@NotNull, @NotBlank, @Email) → 400.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        log.error("400 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = isStackTrace ? sw.toString() : "Stack trace not allowed by property";
        return new ApiError(stackTrace, Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage(),
                "Incorrectly made request.", HttpStatus.BAD_REQUEST, LocalDateTime.now().format(formatter));
    }

    /**
     * Обработка ForbiddenException (доступ запрещён) → 403.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(final ForbiddenException e) {
        log.error("403 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = isStackTrace ? sw.toString() : "Stack trace not allowed by property";
        return new ApiError(stackTrace, e.getMessage(), "Incorrectly made request.",
                HttpStatus.FORBIDDEN, LocalDateTime.now().format(formatter));
    }

    /**
     * Обработка ConditionsNotMetException (условия не выполнены) → 409.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMet(final ConditionsNotMetException e) {
        log.error("409 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = isStackTrace ? sw.toString() : "Stack trace not allowed by property";
        return new ApiError(stackTrace, e.getMessage(), "For the requested operation the conditions are not met.",
                HttpStatus.CONFLICT, LocalDateTime.now().format(formatter));
    }

    /**
     * Обработка MissingServletRequestParameterException (отсутствует параметр) → 400.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameter(final MissingServletRequestParameterException e) {
        log.error("400 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = isStackTrace ? sw.toString() : "Stack trace not allowed by property";
        return new ApiError(stackTrace, e.getMessage(),
                "Incorrectly made request.", HttpStatus.BAD_REQUEST, LocalDateTime.now().format(formatter));
    }

    /**
     * Обработка всех остальных исключений (fallback) → 500.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.error("500 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = isStackTrace ? sw.toString() : "Stack trace not allowed by property";
        return new ApiError(stackTrace, e.getMessage(), "Unknown error.",
                HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now().format(formatter));
    }
}
