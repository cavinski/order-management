package com.caio.ordermanagement.order.exceptions;

public class InvalidOrderItemException extends RuntimeException{
    
    public InvalidOrderItemException(String message) {
        super(message);
    }
}
