package com.caio.ordermanagement.order;

import com.caio.ordermanagement.order.exceptions.InvalidOrderItemException;
import com.caio.ordermanagement.product.Product;
import java.math.BigDecimal;

public class OrderItem {
    
    private final Product product;
    private int quantity;
    private final BigDecimal unitPrice;

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