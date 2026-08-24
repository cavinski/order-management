package com.caio.ordermanagement.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationTest {
    
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidUser() {
        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectBlankName() {
        User user = new User(
            "",
            "caio@example.com",
            "hashed-password"
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("name");
    }

    @Test
    void shouldRejectBlankEmail() {
        User user = new User(
            "Caio",
            "",
            "hashed-password"
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("email");
    }

    @Test
    void shouldRejectInvalidEmail() {
        User user = new User(
            "Caio",
            "invalid-email",
            "hashed-password"
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("email");
    }

    @Test
    void shouldRejectBlankPassword() {
        User user = new User(
            "Caio",
            "caio@example.com",
            ""
        );

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("password");
    }
}
