package com.caio.ordermanagement.product;

import com.caio.ordermanagement.product.exceptions.ProductNotFoundException;
import com.caio.ordermanagement.product.dto.CreateProductRequest;
import com.caio.ordermanagement.product.dto.CreateProductResponse;
import com.caio.ordermanagement.product.dto.UpdateProductDescriptionRequest;
import com.caio.ordermanagement.product.dto.UpdateProductNameRequest;
import com.caio.ordermanagement.product.dto.UpdateProductPriceRequest;
import com.caio.ordermanagement.product.exceptions.InvalidProductException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.Instant;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {

        CreateProductRequest request = new CreateProductRequest(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        CreateProductResponse response = new CreateProductResponse(
            1L,
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00"),
            true,
            Instant.now(),
            Instant.now()
        );

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Notebook"))
            .andExpect(jsonPath("$.description").value("Notebook para trabalho"))
            .andExpect(jsonPath("$.price").value(3500.00))
            .andExpect(jsonPath("$.active").value(true));

        verify(productService).createProduct(any(CreateProductRequest.class));
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {

        CreateProductRequest request = new CreateProductRequest(
            "",
            "",
            new BigDecimal("-1.00")
        );

        mockMvc.perform(post("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void shouldGetProductById() throws Exception {

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Notebook"))
            .andExpect(jsonPath("$.description")
            .value("Notebook para trabalho"))
            .andExpect(jsonPath("$.price").value(3500.00))
            .andExpect(jsonPath("$.active").value(true));

        verify(productService).getProductById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        when(productService.getProductById(1L)).thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(get("/products/1")).andExpect(status().isNotFound());

        verify(productService).getProductById(1L);
    }

    @Test
    void shouldUpdateProductName() throws Exception {

        UpdateProductNameRequest request = new UpdateProductNameRequest("Notebook Pro");

        mockMvc.perform(patch("/products/1/name")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(productService).updateProductName(1L, "Notebook Pro");
    }

    @Test
    void shouldRejectInvalidProductNameUpdate() throws Exception {

        UpdateProductNameRequest request = new UpdateProductNameRequest("");

        mockMvc.perform(patch("/products/1/name")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void shouldUpdateProductDescription() throws Exception {

        UpdateProductDescriptionRequest request = 
            new UpdateProductDescriptionRequest("Notebook para desenvolvimento");

        mockMvc.perform(patch("/products/1/description")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(productService).updateProductDescription(
            1L,
            "Notebook para desenvolvimento"
        );
    }

    @Test
    void shouldRejectInvalidProductDescriptionUpdate() throws Exception {

        UpdateProductDescriptionRequest request = new UpdateProductDescriptionRequest("");

        mockMvc.perform(patch("/products/1/description")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void shouldUpdateProductPrice() throws Exception {

        UpdateProductPriceRequest request = new UpdateProductPriceRequest(new BigDecimal("4000.00"));

        mockMvc.perform(patch("/products/1/price")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(productService).updateProductPrice(
            1L,
            new BigDecimal("4000.00")
        );
    }

    @Test
    void shouldRejectInvalidProductPriceUpdate() throws Exception {

        UpdateProductPriceRequest request = new UpdateProductPriceRequest(new BigDecimal("-0.01"));

        mockMvc.perform(patch("/products/1/price")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void shouldDeactivateProduct() throws Exception {

        mockMvc.perform(patch("/products/1/deactivation")).andExpect(status().isNoContent());

        verify(productService).deactivateProduct(1L);
    }
}