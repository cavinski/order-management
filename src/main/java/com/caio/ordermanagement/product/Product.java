package com.caio.ordermanagement.product;

import com.caio.ordermanagement.product.exceptions.InvalidProductException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity 
@Table(name = "products")
public class Product {
 
    private static final int MAX_NAME_LENGTH  = 100;


    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100) 
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2) 
    private BigDecimal price;

    @Column(nullable = false) 
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Product() {}

    public Product(String name, String description, BigDecimal price) {

        validateName(name);
        validateDescription(description);
        validatePrice(price);

        this.name = name;
        this.description = description;
        this.price = price;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }

    public void updateDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void updatePrice(BigDecimal price) {
        validatePrice(price);
        this.price = price;
    }

    public void deactivate() {
        this.active = false;
    }


    private void validateName(String name) {

        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Name cannot be blank");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidProductException("Name cannot exceed 100 characters");
        }
    }

    private void validateDescription(String description) {

        if (description == null || description.isBlank()) {
            throw new InvalidProductException("Description cannot be blank");
        }
    }

    private void validatePrice(BigDecimal price) {
        
        if (price == null) {
            throw new InvalidProductException("Price cannot be null");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductException("Price cannot be negative");
        }
    }
}