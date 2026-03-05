package com.ecommerce.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsService {

    UserDetails loadUserByUsername(String username);

    UserDetails loadUserById(Long userId);
}
