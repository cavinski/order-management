package com.caio.ordermanagement.order;

import com.caio.ordermanagement.product.Product;
import com.caio.ordermanagement.product.ProductRepository;
import com.caio.ordermanagement.user.User;
import com.caio.ordermanagement.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers 
@SpringBootTest 
public class OrderRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired 
    private OrderRepository orderRepository;

    @Autowired 
    private UserRepository userRepository;

    @Autowired 
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional 
    void shouldPersistAndRetrieveOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        userRepository.save(user);

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        productRepository.save(product);

        OrderItem item = new OrderItem(product, 2);

        Order order = new Order(user, List.of(item));

        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getId()).isNotNull();

        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertThat(foundOrder.getUser().getId()).isEqualTo(user.getId());

        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.CREATED);

        assertThat(foundOrder.getItems()).hasSize(1);

        OrderItem foundItem = foundOrder.getItems().get(0);

        assertThat(foundItem.getProduct().getId()).isEqualTo(product.getId());

        assertThat(foundItem.getQuantity()).isEqualTo(2);

        assertThat(foundItem.getUnitPrice()).isEqualByComparingTo("3500.00");

        assertThat(foundItem.isActive()).isTrue();

        assertThat(foundOrder.getTotal()).isEqualByComparingTo("7000.00");

        assertThat(foundOrder.getCreatedAt()).isNotNull();
    }

    @Test
    @Transactional
    void shouldPersistOrderWithMultipleItems() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        userRepository.save(user);

        Product notebook = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Product mouse = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("150.00")
        );

        productRepository.saveAll(List.of(notebook, mouse));

        OrderItem notebookItem = new OrderItem(notebook, 2);
        OrderItem mouseItem = new OrderItem(mouse, 3);

        Order order = new Order(user, List.of(notebookItem, mouseItem));

        Order savedOrder = orderRepository.save(order);

        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertThat(foundOrder.getItems()).hasSize(2);

        assertThat(foundOrder.getTotal()).isEqualByComparingTo("7450.00");
    }

    @Test
    @Transactional
    void shouldPersistUnitPriceSnapshot() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        userRepository.save(user);

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        productRepository.save(product);

        OrderItem item = new OrderItem(product, 2);

        Order order = new Order(user, List.of(item));

        Order savedOrder = orderRepository.save(order);

        product.updatePrice(new BigDecimal("4000.00"));
        productRepository.save(product);

        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        OrderItem foundItem = foundOrder.getItems().get(0);

        assertThat(product.getPrice()).isEqualByComparingTo("4000.00");

        assertThat(foundItem.getUnitPrice()).isEqualByComparingTo("3500.00");

        assertThat(foundItem.getSubtotal()).isEqualByComparingTo("7000.00");

        assertThat(foundOrder.getTotal()).isEqualByComparingTo("7000.00");
    }

    @Test
    @Transactional
    void shouldPersistInactiveOrderItem() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        userRepository.save(user);

        Product notebook = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Product mouse = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("150.00")
        );

        productRepository.saveAll(List.of(notebook, mouse));

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 2);

        Order order = new Order(user, List.of(notebookItem, mouseItem));

        Order savedOrder = orderRepository.save(order);

        savedOrder.removeItem(mouseItem);

        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertThat(foundOrder.getItems()).hasSize(2);

        OrderItem foundMouseItem = foundOrder.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(mouse.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(foundMouseItem.isActive()).isFalse();

        assertThat(foundOrder.getTotal()).isEqualByComparingTo("3500.00");
    }

    @Test
    @Transactional
    void shouldKeepInactiveOrderItemPersisted() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        userRepository.save(user);

        Product notebook = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Product mouse = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("150.00")
        );

        productRepository.saveAll(List.of(notebook, mouse));

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 2);

        Order order = new Order(user, List.of(notebookItem, mouseItem));

        Order savedOrder = orderRepository.save(order);

        savedOrder.removeItem(mouseItem);

        Order foundOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertThat(foundOrder.getItems()).hasSize(2);

        OrderItem foundMouseItem = foundOrder.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(mouse.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(foundMouseItem.isActive()).isFalse();
    }
}