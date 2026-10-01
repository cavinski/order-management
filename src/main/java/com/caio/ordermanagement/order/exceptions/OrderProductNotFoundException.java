package com.caio.ordermanagement.order.exceptions;

public class OrderProductNotFoundException extends RuntimeException{

    public OrderProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
    
}