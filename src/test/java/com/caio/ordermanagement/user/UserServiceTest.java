package com.caio.ordermanagement.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository);
    }

    @Test
    void shouldCreateUser() {

        when(userRepository.existsByEmail("caio@example.com")).thenReturn(false);

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        assertThat(createdUser.getName()).isEqualTo("Caio");
        assertThat(createdUser.getEmail()).isEqualTo("caio@example.com");
        assertThat(createdUser.isActive()).isTrue();

        verify(userRepository).existsByEmail("caio@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        when(userRepository.existsByEmail("caio@example.com")).thenReturn(true);

        assertThatThrownBy(() -> 

            userService.createUser(
                "Caio",
                "caio@example.com",
                "hashed-password"
            )

        ).isInstanceOf(IllegalArgumentException.class).hasMessage("Email already in use");

        verify(userRepository).existsByEmail("caio@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}
