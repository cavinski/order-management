package com.caio.ordermanagement.order;

import com.caio.ordermanagement.order.dto.CreateOrderRequest;
import com.caio.ordermanagement.order.dto.CreateOrderResponse;
import com.caio.ordermanagement.order.dto.GetOrderResponse;
import com.caio.ordermanagement.order.exceptions.OrderNotFoundException;
import com.caio.ordermanagement.order.exceptions.OrderProductNotFoundException;
import com.caio.ordermanagement.order.exceptions.OrderUserNotFoundException;
import com.caio.ordermanagement.product.Product;
import com.caio.ordermanagement.product.ProductRepository;
import com.caio.ordermanagement.user.User;
import com.caio.ordermanagement.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service 
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(
        OrderRepository orderRepository,
        UserRepository userRepository,
        ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional 
    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        User user = userRepository.findById(request.userId()).orElseThrow(() -> 
            new OrderUserNotFoundException(request.userId())
        );

        List<OrderItem> items = new ArrayList<>();

        for (CreateOrderRequest.ItemRequest itemRequest : request.items()) {

            Product product = productRepository.findById(itemRequest.productId()).orElseThrow(()  ->
                new OrderProductNotFoundException(itemRequest.productId())
            );

            items.add(new OrderItem(product, itemRequest.quantity()));
        }

        Order order = new Order(user, items);

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public GetOrderResponse getOrder(Long id) {

        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        return toGetResponse(order);
    }

    @Transactional
    public void confirmOrder(Long id) {

        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        order.confirm();
    }

    @Transactional
    public void startProcessing(Long id) {

        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        order.startProcessing();
    }

    @Transactional
    public void shipOrder(Long id) {

        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

        order.ship();
    }

    private CreateOrderResponse toResponse(Order order) {

        List<CreateOrderResponse.ItemResponse> items =
            order.getItems().stream().map(item -> new CreateOrderResponse.ItemResponse(
                item.getProduct().getId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
            )).toList();

        return new CreateOrderResponse(
            order.getId(),
            order.getUser().getId(),
            order.getStatus(),
            order.getTotal(),
            order.getCreatedAt(),
            items
        );
    }

    private GetOrderResponse toGetResponse(Order order) {

        List<GetOrderResponse.ItemResponse> items =
            order.getItems().stream().map(item -> new GetOrderResponse.ItemResponse(
                item.getProduct().getId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.isActive()
            )).toList();

        return new GetOrderResponse(
            order.getId(),
            order.getUser().getId(),
            order.getStatus(),
            order.getTotal(),
            order.getCreatedAt(),
            items
        );
    }
}