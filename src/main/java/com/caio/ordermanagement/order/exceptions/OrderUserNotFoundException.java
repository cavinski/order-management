package com.caio.ordermanagement.order.exceptions;

public class OrderUserNotFoundException extends RuntimeException{
    
    public OrderUserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }

}