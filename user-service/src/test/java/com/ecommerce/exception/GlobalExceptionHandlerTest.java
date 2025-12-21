package com.ecommerce.exception;

import com.ecommerce.exception.custom_exceptions.InvalidCredentialsException;
import com.ecommerce.exception.custom_exceptions.UserAlreadyExistsException;
import com.ecommerce.exception.custom_exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("409 Conflict — UserAlreadyExistsException")
    void handleUserAlreadyExistsException() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("Пользователь уже существует");

        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExistsException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Пользователь уже существует", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("404 Not Found — UserNotFoundException")
    void handleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("Пользователь не найден");

        ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Пользователь не найден", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("401 Unauthorized — InvalidCredentialsException")
    void handleInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Неверные учетные данные");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentialsException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Неверные учетные данные", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("400 Bad Request — MethodArgumentNotValidException (валидация)")
    void handleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("object", "email", "не может быть пустым"),
                new FieldError("object", "password", "длина должна быть не менее 6")
        ));

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("email: не может быть пустым"));
        assertTrue(response.getBody().getMessage().contains("password: длина должна быть не менее 6"));
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("500 Internal Server Error — общий fallback")
    void handleAllUncaughtException() {
        Exception ex = new Exception("Неизвестная ошибка");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Неизвестная ошибка", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }
}