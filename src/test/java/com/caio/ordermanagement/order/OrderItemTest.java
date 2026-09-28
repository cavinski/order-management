package com.caio.ordermanagement.order;

import org.junit.jupiter.api.Test;
import com.caio.ordermanagement.product.Product;
import java.math.BigDecimal;
import com.caio.ordermanagement.order.exceptions.InvalidOrderItemException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderItemTest {
    
    @Test 
    void shouldCreateOrderItem() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        OrderItem item = new OrderItem(product, 2);

        assertThat(item.getProduct()).isSameAs(product);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getUnitPrice()).isEqualByComparingTo("3500.00");
    }

    @Test
    void shouldNotCreateOrderItemWithNullProduct() {

        assertThatThrownBy(() -> new OrderItem(null, 2))
            .isInstanceOf(InvalidOrderItemException.class)
            .hasMessage("Product cannot be null");
    }

    @Test
    void shouldNotCreateOrderItemWithZeroQuantity() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        assertThatThrownBy(() -> new OrderItem(product, 0))
            .isInstanceOf(InvalidOrderItemException.class)
            .hasMessage("Quantity must be positive");
    }

    @Test
    void shouldNotCreateOrderItemWithNegativeQuantity() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        assertThatThrownBy(() -> new OrderItem(product, -1))
            .isInstanceOf(InvalidOrderItemException.class)
            .hasMessage("Quantity must be positive");
    }

    @Test
    void shouldCalculateSubtotal() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        OrderItem item = new OrderItem(product, 2);

        assertThat(item.getSubtotal()).isEqualByComparingTo("7000.00");
    }

    @Test
    void shouldKeepUnitPriceWhenProductPriceChanges() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        OrderItem item = new OrderItem(product, 2);

        product.updatePrice(new BigDecimal("4000.00"));

        assertThat(product.getPrice()).isEqualByComparingTo("4000.00");

        assertThat(item.getUnitPrice()).isEqualByComparingTo("3500.00");

        assertThat(item.getSubtotal()).isEqualByComparingTo("7000.00");
    }

    @Test
    void shouldUpdateQuantity() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        OrderItem item = new OrderItem(product, 2);

        item.updateQuantity(5);

        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getSubtotal()).isEqualByComparingTo("17500.00");
    }

    @Test
    void shouldNotUpdateQuantityToZero() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        OrderItem item = new OrderItem(product, 2);

        assertThatThrownBy(() -> item.updateQuantity(0))
            .isInstanceOf(InvalidOrderItemException.class)
            .hasMessage("Quantity must be positive");
    }

    @Test
    void shouldNotUpdateQuantityToNegativeValue() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        OrderItem item = new OrderItem(product, 2);

        assertThatThrownBy(() -> item.updateQuantity(-1))
            .isInstanceOf(InvalidOrderItemException.class)
            .hasMessage("Quantity must be positive");
    }
}