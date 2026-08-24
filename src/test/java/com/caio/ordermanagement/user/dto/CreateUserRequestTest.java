package com.caio.ordermanagement.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserRequestTest {
    
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
    void shouldAcceptValidRequest() {

        CreateUserRequest request = new CreateUserRequest(
            "Caio", 
            "caio@example.com",
            "password"
        );

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectBlankName() {

        CreateUserRequest request = new CreateUserRequest(
            "",
            "caio@example.com",
            "password"
        );

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("name");
    }

    @Test
    void shouldRejectNameLongerThan100Characters() {

        CreateUserRequest request = new CreateUserRequest(
            "a".repeat(101),
            "caio@example.com",
            "password"
        );

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("name");
    }

    @Test
    void shouldRejectInvalidEmail() {

        CreateUserRequest request = new CreateUserRequest(
            "Caio",
            "invalid-email",
            "password"
        );

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("email");
    }

    @Test
    void shouldRejectEmailLongerThan254Characters() {

        String email = "a".repeat(243) + "@example.com";

        CreateUserRequest request = new CreateUserRequest(
            "Caio",
            email,
            "password"
        );

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("email");
    }

    @Test
    void shouldRejectBlankPassword() {

        CreateUserRequest request = new CreateUserRequest(
            "Caio",
            "caio@example.com",
            ""
        );

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("password");
    }

    @Test
    void shouldRejectPasswordLongerThan255Characters() {

        CreateUserRequest request = new CreateUserRequest(
            "Caio",
            "caio@example.com",
            "a".repeat(256)
        );

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Object::toString)
            .contains("password");
    }
}