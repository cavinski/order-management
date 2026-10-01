package com.caio.ordermanagement.order;

import com.caio.ordermanagement.order.dto.CreateOrderRequest;
import com.caio.ordermanagement.order.dto.CreateOrderResponse;
import com.caio.ordermanagement.order.dto.GetOrderResponse;
import com.caio.ordermanagement.order.exceptions.InvalidOrderException;
import com.caio.ordermanagement.order.exceptions.OrderNotFoundException;
import com.caio.ordermanagement.order.exceptions.OrderProductNotFoundException;
import com.caio.ordermanagement.order.exceptions.OrderUserNotFoundException;
import com.caio.ordermanagement.product.Product;
import com.caio.ordermanagement.product.ProductRepository;
import com.caio.ordermanagement.user.User;
import com.caio.ordermanagement.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product notebook = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Product mouse = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("150.00")
        );

        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(
                new CreateOrderRequest.ItemRequest(10L, 2),
                new CreateOrderRequest.ItemRequest(20L, 1)
            )
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(productRepository.findById(10L)).thenReturn(Optional.of(notebook));

        when(productRepository.findById(20L)).thenReturn(Optional.of(mouse));

        Order savedOrder = new Order(
            user,
            List.of(
                new OrderItem(notebook, 2),
                new OrderItem(mouse, 1)
            )
        );

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        CreateOrderResponse response = orderService.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getUser()).isSameAs(user);

        assertThat(capturedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);

        assertThat(capturedOrder.getItems()).hasSize(2);

        OrderItem capturedNotebookItem = capturedOrder.getItems().get(0);
        OrderItem capturedMouseItem = capturedOrder.getItems().get(1);

        assertThat(capturedNotebookItem.getProduct()).isSameAs(notebook);

        assertThat(capturedNotebookItem.getQuantity()).isEqualTo(2);

        assertThat(capturedNotebookItem.getUnitPrice()).isEqualByComparingTo("3500.00");

        assertThat(capturedMouseItem.getProduct()).isSameAs(mouse);

        assertThat(capturedMouseItem.getQuantity()).isEqualTo(1);

        assertThat(capturedMouseItem.getUnitPrice()).isEqualByComparingTo("150.00");

        assertThat(capturedOrder.getTotal()).isEqualByComparingTo("7150.00");

        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);

        assertThat(response.total()).isEqualByComparingTo("7150.00");
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {

        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(
                new CreateOrderRequest.ItemRequest(10L, 2)
            )
        );

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(OrderUserNotFoundException.class)
            .hasMessage("User not found with id: 1");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(
                new CreateOrderRequest.ItemRequest(10L, 2)
            )
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(OrderProductNotFoundException.class)
            .hasMessage("Product not found with id: 10");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldNotCreateOrderForInactiveUser() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        user.deactivate();

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(
                new CreateOrderRequest.ItemRequest(10L, 1)
            )
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Only active users can create orders");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldNotCreateOrderWithInactiveProduct() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        product.deactivate();

        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(
                new CreateOrderRequest.ItemRequest(10L, 1)
            )
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Inactive products cannot be added to orders");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldGetOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product notebook = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Product mouse = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("150.00")
        );

        OrderItem notebookItem = new OrderItem(notebook, 2);
        OrderItem mouseItem = new OrderItem(mouse, 1);

        Order order = new Order(
            user,
            List.of(notebookItem, mouseItem)
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        GetOrderResponse response = orderService.getOrder(1L);

        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.total()).isEqualByComparingTo("7150.00");
        assertThat(response.createdAt()).isEqualTo(order.getCreatedAt());

        assertThat(response.items()).hasSize(2);

        GetOrderResponse.ItemResponse notebookResponse = response.items().get(0);

        assertThat(notebookResponse.productId()).isEqualTo(notebook.getId());
        assertThat(notebookResponse.quantity()).isEqualTo(2);
        assertThat(notebookResponse.unitPrice()).isEqualByComparingTo("3500.00");
        assertThat(notebookResponse.subtotal()).isEqualByComparingTo("7000.00");
        assertThat(notebookResponse.active()).isTrue();

        GetOrderResponse.ItemResponse mouseResponse = response.items().get(1);

        assertThat(mouseResponse.productId()).isEqualTo(mouse.getId());
        assertThat(mouseResponse.quantity()).isEqualTo(1);
        assertThat(mouseResponse.unitPrice()).isEqualByComparingTo("150.00");
        assertThat(mouseResponse.subtotal()).isEqualByComparingTo("150.00");
        assertThat(mouseResponse.active()).isTrue();

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldConfirmOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.confirmOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenConfirmingOrderThatDoesNotExist() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.confirmOrder(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldNotConfirmOrderWhenItIsNotCreated() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirmOrder(1L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be confirmed from CREATED status");
    }

    @Test
    void shouldStartProcessingOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.startProcessing(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenStartingProcessingOrderThatDoesNotExist() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.startProcessing(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldNotStartProcessingOrderWhenItIsNotConfirmed() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.startProcessing(1L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only start processing from CONFIRMED status");
    }

    @Test
    void shouldShipOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();
        order.startProcessing();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.shipOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenShippingOrderThatDoesNotExist() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.shipOrder(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldNotShipOrderWhenItIsNotProcessing() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.shipOrder(1L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be shipped from PROCESSING status");
    }

    @Test
    void shouldDeliverOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();
        order.startProcessing();
        order.ship();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deliverOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenDeliveringOrderThatDoesNotExist() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deliverOrder(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldNotDeliverOrderWhenItIsNotShipped() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();
        order.startProcessing();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.deliverOrder(1L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be delivered from SHIPPED status");
    }

    @Test
    void shouldCancelCreatedOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldCancelConfirmedOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldCancelProcessingOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();
        order.startProcessing();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldThrowWhenCancellingOrderThatDoesNotExist() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldNotCancelShippedOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();
        order.startProcessing();
        order.ship();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be cancelled from CREATED, CONFIRMED or PROCESSING status");
    }

    @Test
    void shouldNotCancelDeliveredOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be cancelled from CREATED, CONFIRMED or PROCESSING status");
    }

    @Test
    void shouldNotCancelAlreadyCancelledOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Notebook",
            "Notebook para trabalho",
            new BigDecimal("3500.00")
        );

        Order order = new Order(
            user,
            List.of(new OrderItem(product, 1))
        );

        order.cancel();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be cancelled from CREATED, CONFIRMED or PROCESSING status");
    }
}