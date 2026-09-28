package com.caio.ordermanagement.order;

import com.caio.ordermanagement.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.caio.ordermanagement.order.exceptions.InvalidOrderException;

public class Order {
    
    private final User user;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Instant createdAt;

    public Order(User user, List<OrderItem> items) {

        if (user == null) {
            throw new InvalidOrderException("User cannot be null");
        }

        if (!user.isActive()) {
            throw new InvalidOrderException("Only active users can create orders");
        }

        validateItems(items);

        for (OrderItem item : items) {
            validateProductIsActive(item);
        }

        this.user = user;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public User getUser() {
        return user;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotal() {
        return items.stream()
            .filter(OrderItem::isActive)
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(OrderItem item) {

        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderException("Order items can only be changed while order is CREATED");
        }

        if (item == null) {
            throw new InvalidOrderException("Order item cannot be null");
        }

        validateProductIsActive(item);
    
        boolean productAlreadyExists = items.stream()
            .anyMatch(existingItem -> existingItem.getProduct() == item.getProduct());

        if (productAlreadyExists) {
            throw new InvalidOrderException("Order cannot contain the same product more than once");
        }

        items.add(item);
    }

    public void removeItem(OrderItem item) {

        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderException("Order items can only be changed while order is CREATED");
        }

        if (item == null) {
            throw new InvalidOrderException("Order item cannot be null");
        }

        if (!items.contains(item)) {
            throw new InvalidOrderException("Order item does not belong to this order");
        }

        long activeItems = items.stream().filter(OrderItem::isActive).count();

        if (activeItems == 1 && item.isActive()) {
            throw new InvalidOrderException("Order must have at least one item");
        }

        item.remove();
    }

    public void updateItemQuantity(OrderItem item, int quantity) {

        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderException("Order items can only be changed while order is CREATED");
        }

        if (item == null) {
            throw new InvalidOrderException("Order item cannot be null");
        }

        if (!items.contains(item)) {
            throw new InvalidOrderException("Order item does not belong to this order");
        }

        if (!item.isActive()) {
            throw new InvalidOrderException("Inactive order items cannot be updated");
        }

        item.updateQuantity(quantity);
    }

    public void confirm() {

        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderException("Order can only be confirmed from CREATED status");
        }

        status = OrderStatus.CONFIRMED;
    }

    public void startProcessing() {

        if (status != OrderStatus.CONFIRMED) {
            throw new InvalidOrderException("Order can only start processing from CONFIRMED status");
        }

        status = OrderStatus.PROCESSING;
    }

    public void ship() {

        if (status != OrderStatus.PROCESSING) {
            throw new InvalidOrderException("Order can only be shipped from PROCESSING status");
        }

        status = OrderStatus.SHIPPED;
    }

    public void deliver() {

        if (status != OrderStatus.SHIPPED) {
            throw new InvalidOrderException("Order can only be delivered from SHIPPED status");
        }

        status = OrderStatus.DELIVERED;
    }

    public void cancel() {

        if (status != OrderStatus.CREATED 
            && status != OrderStatus.CONFIRMED 
            && status != OrderStatus.PROCESSING) {

            throw new InvalidOrderException("Order can only be cancelled from CREATED, CONFIRMED or PROCESSING status");
        }

        status = OrderStatus.CANCELLED;
    }


    private void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must have at least one item");
        }

        long distinctProducts = items.stream().map(OrderItem::getProduct).distinct().count();

        if (distinctProducts != items.size()) {
            throw new InvalidOrderException(
                "Order cannot contain the same product more than once"
            );
        }
    }

    private void validateProductIsActive(OrderItem item) {
        if (!item.getProduct().isActive()) {
            throw new InvalidOrderException("Inactive products cannot be added to orders");
        }
    }
}