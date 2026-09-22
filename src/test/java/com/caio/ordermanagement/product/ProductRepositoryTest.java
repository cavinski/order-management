package com.caio.ordermanagement.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers 
@SpringBootTest 
public class ProductRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductRepository productRepository;


    @Test
    void shouldSaveAndRetrieveProduct() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        Product savedProduct = productRepository.save(product);

        Product foundProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        assertThat(foundProduct.getId()).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("Notebook");
        assertThat(foundProduct.getDescription()).isEqualTo("Notebook para trabalho");
        assertThat(foundProduct.getPrice()).isEqualByComparingTo("3500.00");
        assertThat(foundProduct.isActive()).isTrue();
        assertThat(foundProduct.getCreatedAt()).isNotNull();
        assertThat(foundProduct.getUpdatedAt()).isNotNull();
    }
}
