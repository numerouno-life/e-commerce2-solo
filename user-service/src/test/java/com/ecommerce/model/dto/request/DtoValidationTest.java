package com.ecommerce.model.dto.request;

import com.ecommerce.model.dto.response.UserResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void userLoginRequest_ShouldHaveValidationErrors_WhenLoginIsNull() {
        UserLoginRequest dto = UserLoginRequest.builder()
                .login(null)
                .password("pass")
                .build();

        Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Login не может быть пустым");
    }

    @Test
    void userLoginRequest_ShouldHaveValidationErrors_WhenPasswordIsNull() {
        UserLoginRequest dto = UserLoginRequest.builder()
                .login("user")
                .password(null)
                .build();

        Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Пароль не может быть пустым");
    }

    @Test
    void userLoginRequest_ShouldBeValid_WhenAllFieldsCorrect() {
        UserLoginRequest dto = UserLoginRequest.builder()
                .login("user")
                .password("pass")
                .build();

        Set<ConstraintViolation<UserLoginRequest>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void userRegistrationRequest_ShouldHaveError_WhenUsernameTooShort() {
        UserRegistrationRequest dto = UserRegistrationRequest.builder()
                .username("ab")
                .email("test@example.com")
                .password("secret123")
                .build();

        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Длина ника пользователя не может быть меньше 3 и больше 50 символов");
    }

    @Test
    void userRegistrationRequest_ShouldHaveError_WhenEmailInvalid() {
        UserRegistrationRequest dto = UserRegistrationRequest.builder()
                .username("user")
                .email("not-an-email")
                .password("secret123")
                .build();

        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Некорректный email");
    }

    @Test
    void userRegistrationRequest_ShouldHaveError_WhenPasswordTooShort() {
        UserRegistrationRequest dto = UserRegistrationRequest.builder()
                .username("user")
                .email("test@example.com")
                .password("123")
                .build();

        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Длина пароля не может быть меньше 8 символов");
    }

    @Test
    void userRegistrationRequest_ShouldHaveError_WhenPhoneInvalid() {
        UserRegistrationRequest dto = UserRegistrationRequest.builder()
                .username("user")
                .email("test@example.com")
                .password("secret123")
                .phoneNumber("invalid-phone")
                .build();

        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Номер телефона должен соответствовать паттерну +7 (ХХХ) ХХХ-ХХ-ХХ или быть записан без дополнительных символов и пробелов");
    }

    @Test
    void userRegistrationRequest_ShouldBeValid_WhenAllFieldsCorrect() {
        UserRegistrationRequest dto = UserRegistrationRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("secret1234")
                .phoneNumber("+7 (999) 999-99-99")
                .build();

        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void userUpdateRequest_ShouldHaveError_WhenFirstNameTooLong() {
        UserUpdateRequest dto = UserUpdateRequest.builder()
                .firstName("a".repeat(101))
                .build();

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Длина имени не может быть больше 100 символов");
    }

    @Test
    void userUpdateRequest_ShouldHaveError_WhenPhoneInvalid() {
        UserUpdateRequest dto = UserUpdateRequest.builder()
                .phoneNumber("invalid")
                .build();

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Номер телефона должен соответствовать паттерну +7 (ХХХ) ХХХ-ХХ-ХХ или быть записан без дополнительных символов и пробелов");
    }

    @Test
    void userUpdateRequest_ShouldBeValid_WhenAllFieldsCorrect() {
        UserUpdateRequest dto = UserUpdateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+7 (999) 999-99-99")
                .build();

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void userResponse_CanBeBuiltAndAccessed() {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("test")
                .email("test@test.com")
                .role("USER")
                .createdAt(java.time.LocalDateTime.now())
                .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("test");
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getRole()).isEqualTo("USER");
    }
}
