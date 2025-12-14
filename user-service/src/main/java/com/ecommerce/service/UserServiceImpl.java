package com.ecommerce.service;

import com.ecommerce.exceptions.UserAlreadyExistsException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.response.UserResponse;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering user: {}", request);
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("Ошибка регистрации: имя пользователя '{}' уже занято", request.getUsername());
            throw new UserAlreadyExistsException("Пользователь с именем '" + request.getUsername() + "' уже существует");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("Ошибка регистрации: email '{}' уже зарегистрирован", request.getEmail());
            throw new UserAlreadyExistsException("Пользователь с email '" + request.getEmail() + "' уже существует");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build();
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser);

        return userMapper.toDto(savedUser);
    }
}
