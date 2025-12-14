package com.ecommerce.config;

import com.ecommerce.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    public String generateToken(User user) {

    }

    public boolean validateToken(String token) {

    }

    public String getUserIdFromToken(String token) {

    }

    public String getUsernameFromToken(String token) {

    }
}
