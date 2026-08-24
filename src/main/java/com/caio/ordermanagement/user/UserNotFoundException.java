package com.caio.ordermanagement.user;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}
