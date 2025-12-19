package com.ecommerce;

import com.ecommerce.exceptions.InvalidCredentialsException;
import com.ecommerce.exceptions.UserAlreadyExistsException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.model.dto.request.UserLoginRequest;
import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.response.AuthResponse;
import com.ecommerce.model.dto.response.UserResponse;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import com.ecommerce.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserLoginRequest validLoginRequest;
    private UserLoginRequest validEmailRequest;
    private UserLoginRequest invalidPasswordRequest;
    private UserLoginRequest nonExistentUserRequest;

    @BeforeEach
    void setUp() {
        // Создание тестового пользователя
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword123")
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Валидные запросы
        validLoginRequest = UserLoginRequest.builder()
                .login("testuser")
                .password("rawPassword123")
                .build();

        validEmailRequest = UserLoginRequest.builder()
                .login("test@example.com")
                .password("rawPassword123")
                .build();

        // Неверный пароль
        invalidPasswordRequest = UserLoginRequest.builder()
                .login("testuser")
                .password("wrongPassword")
                .build();

        // Несуществующий пользователь
        nonExistentUserRequest = UserLoginRequest.builder()
                .login("nonexistent")
                .password("anyPassword")
                .build();
    }

    @Test
    void registerSuccess() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "testUser", "john.doe@example.com", "password123",
                "John", "Doe", "1122334455"
        );

        User savedUser = User.builder()
                .id(1L)
                .username("testUser")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1122334455")
                .role(User.UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .username("testUser")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1122334455")
                .role(User.UserRole.USER.toString())
                .createdAt(savedUser.getCreatedAt())
                .build();

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(userMapper.toDto(savedUser)).thenReturn(expectedResponse);

        UserResponse result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(userRepository).existsByUsername("testUser");
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(savedUser);
    }

    @Test
    public void registerDuplicateUsername() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "testUser", "john.doe@example.com", "password123",
                "John", "Doe", "1122334455"
        );
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("Пользователь с именем '" + request.getUsername() + "' уже существует",
                exception.getMessage());
        verify(userRepository).existsByUsername("testUser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    public void registerDuplicateEmail() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "newUser", "existing@example.com", "password123",
                "John", "Doe", "1122334455"
        );

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("Пользователь с email '" + request.getEmail() + "' уже существует", exception.getMessage());
        verify(userRepository).existsByUsername("newUser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }


    @Test
    void authenticateUser_WithValidUsername_ShouldReturnAuthResponse() {
        String expectedToken = "jwt.token.here";

        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("rawPassword123", "encodedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn(expectedToken);

        AuthResponse response = userService.authenticateUser(validLoginRequest);

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getUsername(), response.getUsername());

        verify(userRepository, times(1)).findByEmail("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("rawPassword123", "encodedPassword123");
        verify(jwtTokenProvider, times(1)).generateToken(testUser);
    }

    @Test
    void authenticateUser_WithValidEmail_ShouldReturnAuthResponse() {
        String expectedToken = "jwt.token.here";

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("rawPassword123", "encodedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn(expectedToken);

        AuthResponse response = userService.authenticateUser(validEmailRequest);

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getUsername(), response.getUsername());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).findByUsername(anyString());
        verify(passwordEncoder, times(1)).matches("rawPassword123", "encodedPassword123");
        verify(jwtTokenProvider, times(1)).generateToken(testUser);
    }

    @Test
    void authenticateUser_WhenUserNotFound_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.authenticateUser(nonExistentUserRequest)
        );

        assertEquals("Неверные учетные данные", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("nonexistent");
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void authenticateUser_WhenPasswordIncorrect_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword123")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.authenticateUser(invalidPasswordRequest)
        );

        assertEquals("Неверные учетные данные", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword123");
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void authenticateUser_ShouldCallPasswordEncoderWithCorrectParameters() {
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("rawPassword123", "encodedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("token");

        userService.authenticateUser(validLoginRequest);

        verify(passwordEncoder, times(1)).matches("rawPassword123", "encodedPassword123");
    }

    @Test
    void authenticateUser_ShouldCallJwtTokenProviderWithUserObject() {
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("rawPassword123", "encodedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn("token");

        userService.authenticateUser(validLoginRequest);

        verify(jwtTokenProvider, times(1)).generateToken(testUser);
    }

    @Test
    void authenticateUser_ShouldReturnCorrectAuthResponseStructure() {
        String expectedToken = "generated.jwt.token";
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("rawPassword123", "encodedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn(expectedToken);

        AuthResponse response = userService.authenticateUser(validLoginRequest);

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getUsername(), response.getUsername());
    }

    @Test
    void authenticateUser_WithCaseSensitiveUsername_ShouldWorkCorrectly() {
        UserLoginRequest uppercaseRequest = UserLoginRequest.builder()
                .login("TESTUSER")
                .password("rawPassword123")
                .build();

        when(userRepository.findByEmail("TESTUSER")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("TESTUSER")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticateUser(uppercaseRequest));
    }

    @Test
    void authenticateUser_WithWhitespaceInLogin_ShouldBeTrimmed() {
        UserLoginRequest whitespaceRequest = UserLoginRequest.builder()
                .login("  testuser  ")
                .password("rawPassword123")
                .build();

        when(userRepository.findByEmail("  testuser  ")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("  testuser  ")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticateUser(whitespaceRequest));
    }

    @Test
    void authenticateUser_WhenFoundByEmail_ShouldNotSearchByUsername() {
        String expectedToken = "jwt.token.here";
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("rawPassword123", "encodedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateToken(testUser)).thenReturn(expectedToken);

        userService.authenticateUser(validEmailRequest);

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).findByUsername(anyString());
    }
}