package com.caio.ordermanagement.order;

import com.caio.ordermanagement.order.exceptions.InvalidOrderItemException;
import com.caio.ordermanagement.product.Product;
import java.math.BigDecimal;

public class OrderItem {
    
    private final Product product;
    private int quantity;
    private final BigDecimal unitPrice;
    private boolean active;

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