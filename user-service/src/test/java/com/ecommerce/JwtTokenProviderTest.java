package com.ecommerce;

import com.ecommerce.model.entity.User;
import com.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    public void testGenerateAndParseToken() {
        User user = User.builder()
                .id(1L)
                .username("testUser")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(User.UserRole.USER)
                .createdAt(LocalDateTime.now())
                .build();

        String token = jwtTokenProvider.generateToken(user);

        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testUser", jwtTokenProvider.getUsernameFromToken(token));
        assertEquals(1L, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void testGetUserIdFromInvalidToken() {
        assertNull(jwtTokenProvider.getUserIdFromToken("invalid"));
    }
}
