package com.caio.ordermanagement.order;

import com.caio.ordermanagement.order.exceptions.InvalidOrderException;
import com.caio.ordermanagement.order.exceptions.OrderProductNotFoundException;
import com.caio.ordermanagement.order.exceptions.OrderUserNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class OrderExceptionHandler {
    
    @ExceptionHandler(InvalidOrderException.class)
    public ProblemDetail handleInvalidOrder(InvalidOrderException exception) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Invalid order");
        problem.setDetail(exception.getMessage());

        return problem;
    }

    @ExceptionHandler(OrderUserNotFoundException.class)
    public ProblemDetail handleOrderUserNotFound(OrderUserNotFoundException exception) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("User not found");
        problem.setDetail(exception.getMessage());

        return problem;
    }

    @ExceptionHandler(OrderProductNotFoundException.class)
    public ProblemDetail handleOrderProductNotFound(OrderProductNotFoundException exception) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Product not found");
        problem.setDetail(exception.getMessage());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Validation failed");
        problem.setDetail("One or more fields are invalid.");

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));

        problem.setProperty("errors", errors);

        return problem;
    }
}