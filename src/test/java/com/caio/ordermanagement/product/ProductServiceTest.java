package com.caio.ordermanagement.product;

import com.caio.ordermanagement.product.dto.CreateProductRequest;
import com.caio.ordermanagement.product.dto.CreateProductResponse;
import com.caio.ordermanagement.product.exceptions.InvalidProductException;
import com.caio.ordermanagement.product.exceptions.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;


    @Test
    void shouldCreateProduct() {

        CreateProductRequest request = new CreateProductRequest(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        Product product = new Product(
                request.name(),
                request.description(),
                request.price()
        );

        when(productRepository.save(any(Product.class))).thenReturn(product);

        CreateProductResponse response = productService.createProduct(request);

        assertThat(response.name()).isEqualTo("Notebook");
        assertThat(response.description()).isEqualTo("Notebook para trabalho");
        assertThat(response.price()).isEqualByComparingTo("3500.00");
        assertThat(response.active()).isTrue();

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldDeactivateProduct() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deactivateProduct(1L);

        assertThat(product.isActive()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExistOnDeactivation() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deactivateProduct(1L))
            .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldGetProductById() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product foundProduct = productService.getProductById(1L);

        assertThat(foundProduct).isSameAs(product);
        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExistOnGetById() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->productService.getProductById(1L))
            .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldUpdateProductName() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.updateProductName(1L, "Notebook Pro");

        assertThat(product.getName()).isEqualTo("Notebook Pro");
        verify(productRepository).findById(1L);
    }

    @Test
    void shouldRejectInvalidNameWhenUpdatingProductName() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProductName(1L, ""))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Name cannot be blank");
    }

    @Test
    void shouldRejectNameLongerThan100CharactersWhenUpdatingProductName() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        String name = "a".repeat(101);

        assertThatThrownBy(() -> productService.updateProductName(1L, name))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Name cannot exceed 100 characters");
    }

    @Test
    void shouldUpdateProductDescription() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.updateProductDescription(1L, "Notebook para desenvolvimento");

        assertThat(product.getDescription()).isEqualTo("Notebook para desenvolvimento");

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldRejectInvalidDescriptionWhenUpdatingProductDescription() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProductDescription(1L, ""))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Description cannot be blank");
    }

    @Test
    void shouldUpdateProductPrice() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.updateProductPrice(1L, new BigDecimal("4000.00"));

        assertThat(product.getPrice()).isEqualByComparingTo("4000.00");

        verify(productRepository).findById(1L);
    }

    @Test
    void shouldRejectNegativePriceWhenUpdatingProductPrice() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProductPrice(1L, new BigDecimal("-0.01")))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Price cannot be negative");
    }

    @Test
    void shouldRejectNullPriceWhenUpdatingProductPrice() {

        Product product = new Product(
                "Notebook",
                "Notebook para trabalho",
                new BigDecimal("3500.00")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProductPrice(1L, null))
            .isInstanceOf(InvalidProductException.class)
            .hasMessage("Price cannot be null");
    }

}