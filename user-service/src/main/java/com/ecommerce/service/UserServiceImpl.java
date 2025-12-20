package com.ecommerce.service;

import com.ecommerce.exceptions.InvalidCredentialsException;
import com.ecommerce.exceptions.UserAlreadyExistsException;
import com.ecommerce.exceptions.UserNotFoundException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.model.dto.request.UserLoginRequest;
import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.request.UserUpdateRequest;
import com.ecommerce.model.dto.response.AuthResponse;
import com.ecommerce.model.dto.response.UserResponse;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Override
    @Transactional(readOnly = true)
    public AuthResponse authenticateUser(UserLoginRequest request) {
        log.info("Аутентификация пользователя c login: {}", request.getLogin());
        String login = request.getLogin();
        Optional<User> userOpt = userRepository.findByEmail(login).or(() -> userRepository.findByUsername(login));

        User user = userOpt
                .orElseThrow(() -> new InvalidCredentialsException("Неверные учетные данные"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Неудачная попытка входа: неверный пароль для login '{}'", login);
            throw new InvalidCredentialsException("Неверные учетные данные");
        }

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        log.info("Получение профиля пользователя с ID: {}", userId);
        User user = findUserById(userId);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        log.info("Обновление профиля пользователя с ID: {}", userId);
        User user = findUserById(userId);
        userMapper.updateEntityFromDto(request, user);
        User savedUser = userRepository.save(user);
        UserResponse userResponse = userMapper.toDto(savedUser);
        log.info("Профиль пользователя обновлен: {}", userResponse);
        return userResponse;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));
    }
}
