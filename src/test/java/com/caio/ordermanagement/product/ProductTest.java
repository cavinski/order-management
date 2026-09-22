package com.caio.ordermanagement.product;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.caio.ordermanagement.product.exceptions.InvalidProductException;

public class ProductTest {
    
    @Test 
    void shouldCreateProduct() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        assertThat(product.getName()).isEqualTo("Notebook");
        assertThat(product.getDescription()).isEqualTo("Notebook para trabalho");
        assertThat(product.getPrice()).isEqualByComparingTo("3500.00");
        assertThat(product.isActive()).isTrue();
    }

    @Test
    void shouldRejectBlankName() {

        assertThatThrownBy(() -> new Product(
            "",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        ))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Name cannot be blank");
    }

    @Test
    void shouldRejectNullName() {

        assertThatThrownBy(() -> new Product(
            null,
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        ))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Name cannot be blank");
    }

    @Test
    void shouldRejectNameLongerThan100Characters() {

        String name = "a".repeat(101);

        assertThatThrownBy(() -> new Product(
            name,
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        ))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Name cannot exceed 100 characters");
    }

    @Test
    void shouldRejectBlankDescription() {

        assertThatThrownBy(() -> new Product(
            "Notebook",
            "",
            new BigDecimal("3500.00")
        ))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Description cannot be blank");
    }

    @Test
    void shouldRejectNullDescription() {

        assertThatThrownBy(() -> new Product(
            "Notebook",
            null,
            new BigDecimal("3500.00")
        ))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Description cannot be blank");
    }

    @Test
    void shouldRejectNegativePrice() {

        assertThatThrownBy(() -> new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("-0.01")
        ))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Price cannot be negative");
    }

    @Test
    void shouldAcceptZeroPrice() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            BigDecimal.ZERO
        );

        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldUpdateName() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        product.updateName("Notebook Pro");

        assertThat(product.getName()).isEqualTo("Notebook Pro");
    }

    @Test
    void shouldRejectBlankNameWhenUpdating() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        assertThatThrownBy(() -> product.updateName(""))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Name cannot be blank");
    }

    @Test
    void shouldRejectNameLongerThan100CharactersWhenUpdating() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        String name = "a".repeat(101);

        assertThatThrownBy(() -> product.updateName(name))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Name cannot exceed 100 characters");
    }

    @Test
    void shouldUpdateDescription() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        product.updateDescription("Notebook para desenvolvimento");

        assertThat(product.getDescription())
            .isEqualTo("Notebook para desenvolvimento");
    }

    @Test
    void shouldRejectBlankDescriptionWhenUpdating() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        assertThatThrownBy(() -> product.updateDescription(""))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Description cannot be blank");
    }

    @Test
    void shouldRejectNullDescriptionWhenUpdating() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        assertThatThrownBy(() -> product.updateDescription(null))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Description cannot be blank");
    }

    @Test
    void shouldUpdatePrice() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        product.updatePrice(new BigDecimal("4000.00"));

        assertThat(product.getPrice())
            .isEqualByComparingTo("4000.00");
    }

    @Test
    void shouldRejectNegativePriceWhenUpdating() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        assertThatThrownBy(() -> product.updatePrice(new BigDecimal("-0.01")))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Price cannot be negative");
    }

    @Test
    void shouldRejectNullPriceWhenUpdating() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        assertThatThrownBy(() -> product.updatePrice(null))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Price cannot be null");
    }

    @Test
    void shouldDeactivateProduct() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        product.deactivate();

        assertThat(product.isActive()).isFalse();
    }

    @Test
    void shouldRemainInactiveWhenDeactivatedMoreThanOnce() {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        product.deactivate();
        product.deactivate();

        assertThat(product.isActive()).isFalse();
    }
}