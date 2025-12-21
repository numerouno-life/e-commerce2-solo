package com.ecommerce.mapper;

import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.request.UserUpdateRequest;
import com.ecommerce.model.dto.response.UserResponse;
import com.ecommerce.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toDto_ShouldMapAllFieldsCorrectly() {
        User user = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .role(User.UserRole.USER)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        UserResponse response = userMapper.toDto(user);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getPhoneNumber()).isEqualTo("1234567890");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }

    @Test
    void toEntity_ShouldMapAllFieldsCorrectly() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("jane_doe")
                .email("jane@example.com")
                .password("secret123")
                .firstName("Jane")
                .lastName("Doe")
                .phoneNumber("0987654321")
                .build();

        User user = userMapper.toEntity(request);

        assertThat(user.getUsername()).isEqualTo("jane_doe");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getPassword()).isEqualTo("secret123");
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getPhoneNumber()).isEqualTo("0987654321");
        assertThat(user.getRole()).isEqualTo(User.UserRole.USER);
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {
        User user = User.builder()
                .firstName("Old")
                .lastName("Name")
                .phoneNumber("1111111111")
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("New")
                .phoneNumber(null) // не обновляем
                .build();

        // When
        userMapper.updateEntityFromDto(request, user);

        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("Name"); // не изменилось
        assertThat(user.getPhoneNumber()).isEqualTo("1111111111"); // не изменилось
    }

    @Test
    void updateEntityFromDto_ShouldDoNothingWhenDtoIsNullFields() {
        User user = User.builder()
                .firstName("Old")
                .build();

        UserUpdateRequest request = new UserUpdateRequest(); // все поля null
        userMapper.updateEntityFromDto(request, user);
        assertThat(user.getFirstName()).isEqualTo("Old");
    }
}

