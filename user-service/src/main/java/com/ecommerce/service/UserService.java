package com.ecommerce.service;

import com.ecommerce.model.dto.request.UserLoginRequest;
import com.ecommerce.model.dto.request.UserRegistrationRequest;
import com.ecommerce.model.dto.response.AuthResponse;
import com.ecommerce.model.dto.response.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRegistrationRequest request);

    AuthResponse authenticateUser(UserLoginRequest request);
}
