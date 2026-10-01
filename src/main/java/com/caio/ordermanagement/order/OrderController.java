package com.caio.ordermanagement.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.caio.ordermanagement.order.dto.CreateOrderRequest;
import com.caio.ordermanagement.order.dto.CreateOrderResponse;
import com.caio.ordermanagement.order.dto.GetOrderResponse;

@RestController 
@RequestMapping("/orders")
public class OrderController {
    
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public GetOrderResponse getOrder(@PathVariable Long id) {
        
        return orderService.getOrder(id);
    }
} 