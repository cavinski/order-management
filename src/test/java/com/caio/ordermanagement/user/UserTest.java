package com.caio.ordermanagement.user;

import org.junit.jupiter.api.Test;

import com.caio.ordermanagement.user.exceptions.InvalidUserException;

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

}