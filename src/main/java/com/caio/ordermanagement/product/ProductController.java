package com.caio.ordermanagement.product;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.caio.ordermanagement.product.dto.CreateProductRequest;
import com.caio.ordermanagement.product.dto.CreateProductResponse;
import com.caio.ordermanagement.product.dto.GetProductResponse;
import com.caio.ordermanagement.product.dto.UpdateProductDescriptionRequest;
import com.caio.ordermanagement.product.dto.UpdateProductNameRequest;
import com.caio.ordermanagement.product.dto.UpdateProductPriceRequest;

@RestController 
@RequestMapping("/products") 
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping 
    public ResponseEntity<CreateProductResponse> createProduct(
        @Valid @RequestBody CreateProductRequest request) {

            CreateProductResponse response = productService.createProduct(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping ("{id}")
    public ResponseEntity<GetProductResponse> getProductById(@PathVariable Long id) {

        Product product = productService.getProductById(id);

        GetProductResponse response = new GetProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.isActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/name")
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    public void updateProductName(
        @PathVariable Long id, 
        @Valid @RequestBody UpdateProductNameRequest request) {

            productService.updateProductName(id, request.name());
    }

    @PatchMapping("/{id}/description")
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    public void updateProductDescription(
        @PathVariable Long id, 
        @Valid @RequestBody UpdateProductDescriptionRequest request) {

            productService.updateProductDescription(id, request.description());
    }

    @PatchMapping("/{id}/price")
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    public void updateProductPrice(
        @PathVariable Long id, 
        @Valid @RequestBody UpdateProductPriceRequest request) {

            productService.updateProductPrice(id, request.price());
    }

    @PatchMapping("/{id}/deactivation") 
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    public void deactivateProduct(@PathVariable Long id) {

        productService.deactivateProduct(id);
    }
    
}