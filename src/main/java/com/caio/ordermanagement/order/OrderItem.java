package com.caio.ordermanagement.order;

import com.caio.ordermanagement.order.exceptions.InvalidOrderItemException;
import com.caio.ordermanagement.product.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity 
@Table(name = "order_items")
public class OrderItem {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false) 
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private boolean active;

    protected OrderItem() {}

    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new InvalidOrderItemException("Product cannot be null");
        }

        if (quantity <= 0) {
            throw new InvalidOrderItemException("Quantity must be positive");
        }

        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public boolean isActive() {
        return active;
    }

    public void remove() {
        this.active = false;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void updateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidOrderItemException("Quantity must be positive");
        }

        this.quantity = quantity;
    }
}