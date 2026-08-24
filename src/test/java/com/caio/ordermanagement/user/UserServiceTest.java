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

import java.util.Optional;

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

        )   
            .isInstanceOf(EmailAlreadyInUseException.class)
            .hasMessage("Email already in use: caio@example.com");

        verify(userRepository).existsByEmail("caio@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeactivateUser() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertThat(user.isActive()).isFalse();

        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingNonExistentUser() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(1L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found: 1");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void shouldUpdateUserName() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateUserName(1L, "Caio Pedro");

        assertThat(user.getName()).isEqualTo("Caio Pedro");

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameOfNonExistentUser() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserName(1L, "Caio Pedro"))   
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found: 1");

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldGetUserById() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(1L);

        assertThat(foundUser).isSameAs(user);

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentUser() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

         assertThatThrownBy(() -> userService.deactivateUser(1L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found: 1");

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldGetUserByEmail() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.findByEmail("caio@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByEmail("caio@example.com");

        assertThat(foundUser).isSameAs(user);

        verify(userRepository).findByEmail("caio@example.com");
    }

    @Test
    void shouldThrowExceptionWhenGettingUserByNonExistentEmail() {

        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

         assertThatThrownBy(() -> userService.getUserByEmail(email))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found with email: " + email);

        verify(userRepository).findByEmail(email);
    }
}
