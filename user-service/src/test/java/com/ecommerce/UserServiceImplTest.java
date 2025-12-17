package com.ecommerce;

import com.ecommerce.exceptions.UserAlreadyExistsException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.response.UserResponse;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

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

    @InjectMocks
    private UserServiceImpl userService;

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
}
