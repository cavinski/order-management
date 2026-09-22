package com.caio.ordermanagement.product;

import com.caio.ordermanagement.product.dto.CreateProductRequest;
import com.caio.ordermanagement.product.dto.CreateProductResponse;
import com.caio.ordermanagement.product.exceptions.ProductNotFoundException;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class ProductService {
 
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    
    @Transactional 
    public CreateProductResponse createProduct(CreateProductRequest request) {

        Product product = new Product(
            request.name(),
            request.description(),
            request.price()
        );

        Product createdProduct = productRepository.save(product);

        return new CreateProductResponse(
            createdProduct.getId(),
            createdProduct.getName(),
            createdProduct.getDescription(),
            createdProduct.getPrice(),
            createdProduct.isActive(),
            createdProduct.getCreatedAt(),
            createdProduct.getUpdatedAt()
        );
    }

    @Transactional 
    public void deactivateProduct(Long id) {

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        product.deactivate();
    }

    @Transactional(readOnly = true) 
    public Product getProductById(Long id) {

        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional 
    public void updateProductName(Long id, String name) {

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        product.updateName(name);
    }

    @Transactional 
    public void updateProductDescription(Long id, String description) {

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        product.updateDescription(description);
    }

    @Transactional 
    public void updateProductPrice(Long id, BigDecimal price) {

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
            
        product.updatePrice(price);
    }
}