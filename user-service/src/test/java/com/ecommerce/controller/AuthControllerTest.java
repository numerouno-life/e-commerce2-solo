package com.ecommerce.controller;

import com.ecommerce.model.dto.request.UserLoginRequest;
import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.response.AuthResponse;
import com.ecommerce.model.dto.response.UserResponse;
import com.ecommerce.security.JwtAuthenticationFilter;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class AuthControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    private ResultActions performPost(String url, Object request) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Test
    void shouldReturn201_whenRegistrationSuccess() throws Exception {

        UserRegistrationRequest request = new UserRegistrationRequest(
                "username", "test@example.ru",
                "password", "John", "Doe",
                "+79999999999");

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(new UserResponse());

        performPost("/api/auth/register", request)
                .andExpect(status().isCreated());

        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void shouldReturn400_whenUsernameIsEmpty() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                null, "test@example.ru",
                "password", "John", "Doe",
                "+79999999999"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn400_whenUsernameLengthIsLessThan3() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "12", "test@example.ru",
                "password", "John", "Doe",
                "+79999999999"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn400_whenEmailIsEmpty() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "12124", null,
                "password", "John", "Doe",
                "+79999999999"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn400_whenEmailIsInvalid() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "12124", "testexample.ru",
                "password", "John", "Doe",
                "+79999999999"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn400_whenPasswordIsEmpty() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "12124", "test@example.ru",
                null, "John", "Doe",
                "+79999999999"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn400_whenPasswordLengthIsLessThan8() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "12124", "test@example.ru",
                "pass", "John", "Doe",
                "+79999999999"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn400_whenFirstNameLengthIsBiggerThan100() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "12124", "test@example.ru",
                "pass", "3628k53eSKK2kkNar3c3q9w4J5GEq5N2B9qj2gw25bz5gQ98c5CUxwgb9CWZ2NDCeRq27wu9J49" +
                "5z93R87Xek7u74K7BkJ97Ex3yC", "Doe",
                "+79999999999"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn400_whenLastNameLengthIsBiggerThan100() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "12124", "test@example.ru",
                "pass", "John", "3628k53eSKK2kkNar3c3q9w4J5GEq5N2B9qj2gw25bz5gQ98c5CUxwgb9CW" +
                "Z2NDCeRq27wu9J495z93R87Xek7u74K7BkJ97Ex3yC",
                "+79999999999"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn400_whenPhoneIsInvalid() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "12124", "test@example.ru",
                "password", "John", "Doe",
                "123"
        );

        performPost("/api/auth/register", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn200_whenLoginSuccess() throws Exception {

        UserLoginRequest request = new UserLoginRequest("test@example.ru", "password");

        AuthResponse response = new AuthResponse("token", 1L, "username");

        when(userService.authenticateUser(any(UserLoginRequest.class)))
                .thenReturn(response);

        performPost("/api/auth/login", request)
                .andExpectAll(status().isOk(),
                        jsonPath("$.token").value("token"),
                        jsonPath("$.username").value("username"),
                        jsonPath("$.userId").value(1L));

        verify(userService).authenticateUser(any(UserLoginRequest.class));
    }

    @Test
    void login_shouldReturn400_whenEmailIsEmpty() throws Exception {

        UserLoginRequest request = new UserLoginRequest(null, "password");

        AuthResponse response = new AuthResponse("token", 1L, "username");

        when(userService.authenticateUser(any(UserLoginRequest.class)))
                .thenReturn(response);

        performPost("/api/auth/login", request)
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void login_shouldReturn400_whenPasswordIsEmpty() throws Exception {

        UserLoginRequest request = new UserLoginRequest("test@example.ru", null);

        AuthResponse response = new AuthResponse("token", 1L, "username");

        when(userService.authenticateUser(any(UserLoginRequest.class)))
                .thenReturn(response);

        performPost("/api/auth/login", request)
                .andExpectAll(status().isBadRequest());

        verifyNoInteractions(userService);
    }

}