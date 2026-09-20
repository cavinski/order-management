package com.caio.ordermanagement.user;

import org.junit.jupiter.api.Test;

import com.caio.ordermanagement.user.exceptions.InvalidUserException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {
    
    @Test
    void shouldRejectBlankName() {

        assertThatThrownBy(() -> new User("", "caio@example.com", "hashed-password"))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Name cannot be blank");
    }

    @Test
    void shouldRejectBlankEmail() {

        assertThatThrownBy(() -> new User("Caio", "", "hashed-password"))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Email cannot be blank");
    }

    @Test
    void shouldRejectBlankPassword() {

        assertThatThrownBy(() -> new User("Caio", "caio@example.com", ""))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Password cannot be blank");
    }

    @Test
    void shouldRejectNullName() {

        assertThatThrownBy(() -> new User(null, "caio@example.com", "hashed-password"))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Name cannot be blank");
    }

    @Test
    void shouldRejectNullEmail() {

        assertThatThrownBy(() -> new User("Caio", null, "hashed-password"))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Email cannot be blank");
    }

    @Test
    void shouldRejectNullPassword() {

        assertThatThrownBy(() -> new User("Caio", "caio@example.com", null))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Password cannot be blank");
    }

    @Test
    void shouldRejectInvalidEmail() {

        assertThatThrownBy(() -> new User("Caio", "invalid-email", "hashed-password"))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Invalid email");
    }

    @Test
    void shouldAcceptValidEmail() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        assertThat(user.getEmail()).isEqualTo("caio@example.com");
    }

    @Test
    void shouldRejectNameLongerThan100Characters() {

        String name = "a".repeat(101);

        assertThatThrownBy(() -> new User(name, "caio@example.com", "hashed-password"))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Name cannot exceed 100 characters");
    }

    @Test
    void shouldRejectEmailLongerThan254Characters() {

        String email = "a".repeat(243) + "@example.com";

        assertThat(email).hasSize(255);

        assertThatThrownBy(() -> new User("Caio", email, "hashed-password"))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Email cannot exceed 254 characters");
    }

    @Test
    void shouldRejectPasswordLongerThan255Characters() {

        String password = "a".repeat(256);

        assertThatThrownBy(() -> new User("Caio", "caio@example.com", password))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Password cannot exceed 255 characters");
    }

    @Test
    void shouldUpdateName() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        user.updateName("Carlos");

        assertThat(user.getName()).isEqualTo("Carlos");
    }

    @Test
    void shouldRejectBlankNameWhenUpdating() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        assertThatThrownBy(() -> user.updateName(""))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Name cannot be blank");
    }

    @Test
    void shouldRejectNullNameWhenUpdating() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        assertThatThrownBy(() -> user.updateName(null))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Name cannot be blank");
    }

    @Test
    void shouldRejectNameLongerThan100CharactersWhenUpdating() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        String name = "a".repeat(101);

        assertThatThrownBy(() -> user.updateName(name))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Name cannot exceed 100 characters");
    }

    @Test
    void shouldUpdateEmail() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        user.updateEmail("new@example.com");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldRejectBlankEmailWhenUpdating() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        assertThatThrownBy(() -> user.updateEmail(""))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Email cannot be blank");
    }

    @Test
    void shouldRejectNullEmailWhenUpdating() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        assertThatThrownBy(() -> user.updateEmail(null))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Email cannot be blank");
    }

    @Test
    void shouldRejectInvalidEmailWhenUpdating() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        assertThatThrownBy(() -> user.updateEmail("invalid-email"))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Invalid email");
    }

    @Test
    void shouldRejectEmailLongerThan254CharactersWhenUpdating() {
        
        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        String email = "a".repeat(243) + "@example.com";

        assertThat(email).hasSize(255);

        assertThatThrownBy(() -> user.updateEmail(email))
            .isInstanceOf(InvalidUserException.class)
            .hasMessage("Email cannot exceed 254 characters");
    }

}