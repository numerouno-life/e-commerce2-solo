package com.ecommerce;

import com.ecommerce.exceptions.UserAlreadyExistsException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.response.UserResponse;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private final UserMapper userMapper = new UserMapper();

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                passwordEncoder,
                userMapper
        );
    }

    @Test
    void registerSuccess() {
        // given
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
                .role(User.UserRole.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = userService.registerUser(request);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("john.doe@example.com", result.getEmail());

        verify(userRepository).existsByUsername("testUser");
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
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
}
