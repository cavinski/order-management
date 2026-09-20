package com.caio.ordermanagement.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindUser() {

        User user = new User("Caio", "caio@example.com", "hashed-password");

        User savedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Caio");
        assertThat(foundUser.get().getEmail()).isEqualTo("caio@example.com");
        assertThat(foundUser.get().isActive()).isTrue();
    }

    @Test
    void shouldCheckIfEmailExists() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("caio@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
    }

    @Test
    void shouldFindUserByEmail() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "hashed-password"
        );

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("caio@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Caio");
        assertThat(foundUser.get().getEmail()).isEqualTo("caio@example.com");
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {

        Optional<User> foundUser = userRepository.findByEmail("unknown@example.com");

        assertThat(foundUser).isEmpty();
    }
}