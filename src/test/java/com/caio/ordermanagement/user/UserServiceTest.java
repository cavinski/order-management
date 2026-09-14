package com.caio.ordermanagement.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.caio.ordermanagement.user.exceptions.EmailAlreadyInUseException;
import com.caio.ordermanagement.user.exceptions.UserNotFoundException;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.Optional;
import com.caio.ordermanagement.user.dto.CreateUserRequest;
import com.caio.ordermanagement.user.dto.CreateUserResponse;

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

        CreateUserRequest request = new CreateUserRequest(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        User createdUser = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.existsByEmail("caio@example.com")).thenReturn(false);

        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        CreateUserResponse result = userService.createUser(request);      

        assertThat(result.name()).isEqualTo("Caio");
        assertThat(result.email()).isEqualTo("caio@example.com");
        assertThat(result.active()).isTrue();

        verify(userRepository).existsByEmail("caio@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        CreateUserRequest request = new CreateUserRequest(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.existsByEmail("caio@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))   
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
    void shouldUpdateUserEmail() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        userService.updateUserEmail(1L, "new@example.com");

        assertThat(user.getEmail()).isEqualTo("new@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("new@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToEmailAlreadyInUse() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() ->userService.updateUserEmail(1L, "other@example.com"))
            .isInstanceOf(EmailAlreadyInUseException.class)
            .hasMessage("Email already in use: other@example.com");

        assertThat(user.getEmail()).isEqualTo("caio@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("other@example.com");
    }

    @Test
    void shouldAllowUserToKeepCurrentEmail() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateUserEmail(1L, "caio@example.com");

        assertThat(user.getEmail()).isEqualTo("caio@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingEmailOfNonExistentUser() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->userService.updateUserEmail(1L, "new@example.com"))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("User not found: 1");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
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
