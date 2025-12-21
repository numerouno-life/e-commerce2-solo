package com.ecommerce.security;

import com.ecommerce.exception.custom_exceptions.UserNotFoundException;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        User user = User.builder()
                .username("john")
                .password("encodedPass")
                .role(User.UserRole.USER)
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        UserDetails details = userDetailsService.loadUserByUsername("john");

        assertThat(details.getUsername()).isEqualTo("john");
        assertThat(details.getPassword()).isEqualTo("encodedPass");
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Пользователь не найден: unknown");
    }

    @Test
    void loadUserById_ShouldReturnUserDetails_WhenUserExists() {
        User user = User.builder()
                .username("jane")
                .password("encodedPass")
                .role(User.UserRole.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserById(1L);

        assertThat(details.getUsername()).isEqualTo("jane");
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserById_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserById(999L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Пользователь не найден с ID: 999");
    }
}
