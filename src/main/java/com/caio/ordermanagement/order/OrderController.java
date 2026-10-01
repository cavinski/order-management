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

    @PostMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmOrder(@PathVariable Long id) {

        orderService.confirmOrder(id);
    }

    @PostMapping("/{id}/processing")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startProcessing(@PathVariable Long id) {

        orderService.startProcessing(id);
    }

    @PostMapping("/{id}/ship")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void shipOrder(@PathVariable Long id) {

        orderService.shipOrder(id);
    }

    @PostMapping("/{id}/deliver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deliverOrder(@PathVariable Long id) {

        orderService.deliverOrder(id);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable Long id) {
        
        orderService.cancelOrder(id);
    }
} 