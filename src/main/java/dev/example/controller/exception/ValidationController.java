package dev.example.controller.exception;


import dev.example.entity.Rest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ValidationController {
    @ExceptionHandler(ValidationException.class)
    public Rest<Void> validationException(ValidationException e) {
        log.warn("Resolve [{}: {}]", e.getClass().getSimpleName(), e.getMessage());
        return Rest.failure(400,e.getMessage());
    }
}
