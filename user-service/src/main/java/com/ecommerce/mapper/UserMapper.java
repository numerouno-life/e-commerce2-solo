package com.ecommerce.mapper;

import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.request.UserUpdateRequest;
import com.ecommerce.model.dto.response.UserResponse;
import com.ecommerce.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {

    public UserResponse toDto(User entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phoneNumber(entity.getPhoneNumber())
                .role(entity.getRole().toString())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public User toEntity(UserRegistrationRequest dto) {
        return User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .build();
    }

    public void updateEntityFromDto(UserUpdateRequest dto, User entity) {
        Optional.ofNullable(dto.getFirstName()).ifPresent(entity::setFirstName);
        Optional.ofNullable(dto.getLastName()).ifPresent(entity::setLastName);
        Optional.ofNullable(dto.getPhoneNumber()).ifPresent(entity::setPhoneNumber);
    }
}
