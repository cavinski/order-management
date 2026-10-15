package com.caio.ordermanagement.order;

import com.caio.ordermanagement.order.dto.AddOrderItemRequest;
import com.caio.ordermanagement.order.dto.CreateOrderRequest;
import com.caio.ordermanagement.order.dto.CreateOrderResponse;
import com.caio.ordermanagement.order.dto.GetOrderResponse;
import com.caio.ordermanagement.order.dto.UpdateOrderItemQuantityRequest;
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
    void shouldGetOrderIncludingInactiveItems() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product mouse = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Product keyboard = new Product(
            "Keyboard",
            "Teclado mecânico",
            new BigDecimal("200.00")
        );

        OrderItem mouseItem = new OrderItem(mouse, 1);
        OrderItem keyboardItem = new OrderItem(keyboard, 2);

        Order order = new Order(user, List.of(mouseItem, keyboardItem));

        mouseItem.remove();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        GetOrderResponse response = orderService.getOrder(1L);

        assertThat(response.items()).hasSize(2);

        GetOrderResponse.ItemResponse mouseResponse = response.items().get(0);

        assertThat(mouseResponse.productId()).isEqualTo(mouse.getId());
        assertThat(mouseResponse.active()).isFalse();
        assertThat(mouseResponse.subtotal()).isEqualByComparingTo("100.00");

        GetOrderResponse.ItemResponse keyboardResponse = response.items().get(1);

        assertThat(keyboardResponse.productId()).isEqualTo(keyboard.getId());
        assertThat(keyboardResponse.active()).isTrue();
        assertThat(keyboardResponse.subtotal()).isEqualByComparingTo("400.00");

        assertThat(response.total()).isEqualByComparingTo("400.00");
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

    @Test
    void shouldAddItemToOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product existingProduct = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Product newProduct = new Product(
            "Keyboard",
            "Teclado mecânico",
            new BigDecimal("250.00")
        );

        Order order = new Order(user, List.of(new OrderItem(existingProduct, 1)));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));

        orderService.addItem(1L, new AddOrderItemRequest(2L, 2));

        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotal()).isEqualByComparingTo("600.00");

        OrderItem addedItem = order.getItems().get(1);

        assertThat(addedItem.getProduct()).isSameAs(newProduct);
        assertThat(addedItem.getQuantity()).isEqualTo(2);
        assertThat(addedItem.getUnitPrice()).isEqualByComparingTo("250.00");

        verify(orderRepository).findById(1L);
        verify(productRepository).findById(2L);
    }

    @Test
    void shouldThrowWhenAddingItemToNonExistingOrder() {
        
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderService.addItem(
                999L,
                new AddOrderItemRequest(2L, 1)
            )
        )
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");

        verify(orderRepository).findById(999L);
        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldThrowWhenAddingNonExistingProductToOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product existingProduct = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Order order = new Order(user, List.of(new OrderItem(existingProduct, 1)));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderService.addItem(
                1L,
                new AddOrderItemRequest(999L, 1)
            )
        )
            .isInstanceOf(OrderProductNotFoundException.class)
            .hasMessage("Product not found with id: 999");
    }

    @Test
    void shouldNotAddInactiveProductToOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product existingProduct = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Product inactiveProduct = new Product(
            "Keyboard",
            "Teclado mecânico",
            new BigDecimal("250.00")
        );

        inactiveProduct.deactivate();

        Order order = new Order(user, List.of(new OrderItem(existingProduct, 1)));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(2L)).thenReturn(Optional.of(inactiveProduct));

        assertThatThrownBy(() ->
            orderService.addItem(
                1L,
                new AddOrderItemRequest(2L, 1)
            )
        )
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Inactive products cannot be added to orders");

        assertThat(order.getItems()).hasSize(1);
    }

    @Test
    void shouldNotAddItemToConfirmedOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product existingProduct = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Product newProduct = new Product(
            "Keyboard",
            "Teclado mecânico",
            new BigDecimal("250.00")
        );

        Order order = new Order(user, List.of(new OrderItem(existingProduct, 1)));

        order.confirm();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));

        assertThatThrownBy(() ->
            orderService.addItem(
                1L,
                new AddOrderItemRequest(2L, 1)
            )
        )
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");

        assertThat(order.getItems()).hasSize(1);
    }

    @Test
    void shouldNotAddProductAlreadyPresentInOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Order order = new Order(user, List.of(new OrderItem(product, 1)));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() ->
            orderService.addItem(
                1L,
                new AddOrderItemRequest(2L, 1)
            )
        )
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order cannot contain the same product more than once");

        assertThat(order.getItems()).hasSize(1);
    }

    @Test
    void shouldUpdateOrderItemQuantity() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Order order = new Order(user, List.of(new OrderItem(product, 1)));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        UpdateOrderItemQuantityRequest request = new UpdateOrderItemQuantityRequest(3);

        orderService.updateItemQuantity(
            1L,
            10L,
            request
        );

        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(order.getTotal()).isEqualByComparingTo("300.00");

        verify(orderRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenUpdatingItemOfNonExistingOrder() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderService.updateItemQuantity(
                999L,
                10L,
                new UpdateOrderItemQuantityRequest(3)
            )
        )
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldThrowWhenUpdatingItemNotPresentInOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Order order = new Order(user, List.of(new OrderItem(product, 1)));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
            orderService.updateItemQuantity(
                1L,
                999L,
                new UpdateOrderItemQuantityRequest(3)
            )
        )
            .isInstanceOf(OrderProductNotFoundException.class)
            .hasMessage("Product not found with id: 999");
    }

    @Test
    void shouldNotUpdateItemQuantityWhenOrderIsConfirmed() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Order order = new Order(user, List.of(new OrderItem(product, 1)));

        order.confirm();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        UpdateOrderItemQuantityRequest request = new UpdateOrderItemQuantityRequest(5);

        assertThatThrownBy(() ->
            orderService.updateItemQuantity(
                1L,
                10L,
                request
            )
        )
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldNotUpdateInactiveOrderItem() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        OrderItem item = new OrderItem(product, 1);

        Order order = new Order(user, List.of(item));

        item.remove();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        UpdateOrderItemQuantityRequest request = new UpdateOrderItemQuantityRequest(5);

        assertThatThrownBy(() ->
            orderService.updateItemQuantity(
                1L,
                10L,
                request
            )
        )
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Inactive order items cannot be updated");
    }

    @Test
    void shouldRemoveItem() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Product anotherProduct = new Product(
            "Keyboard",
            "Teclado mecânico",
            new BigDecimal("200.00")
        );

        OrderItem itemToRemove = new OrderItem(product, 1);
        OrderItem remainingItem = new OrderItem(anotherProduct, 1);

        Order order = new Order(user, List.of(itemToRemove, remainingItem));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        orderService.removeItem(1L, 10L);

        assertThat(itemToRemove.isActive()).isFalse();
        assertThat(remainingItem.isActive()).isTrue();
        assertThat(order.getItems()).contains(itemToRemove);
    }

    @Test
    void shouldThrowWhenRemovingItemFromNonexistentOrder() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.removeItem(999L, 10L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessage("Order not found with id: 999");
    }

    @Test
    void shouldThrowWhenRemovingNonexistentProduct() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Order order = new Order(user, List.of(
            new OrderItem(
                new Product(
                    "Mouse",
                    "Mouse sem fio",
                    new BigDecimal("100.00")
                ),
                1
            )
        ));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.removeItem(1L, 999L))
            .isInstanceOf(OrderProductNotFoundException.class)
            .hasMessage("Product not found with id: 999");
    }

    @Test
    void shouldThrowWhenRemovingProductNotPresentInOrder() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product orderProduct = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Product otherProduct = new Product(
            "Keyboard",
            "Teclado mecânico",
            new BigDecimal("200.00")
        );

        Order order = new Order(user, List.of(
            new OrderItem(orderProduct, 1),
            new OrderItem(
                new Product(
                    "Headset",
                    "Headset USB",
                    new BigDecimal("150.00")
                ),
                1
            )
        ));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(10L)).thenReturn(Optional.of(otherProduct));

        assertThatThrownBy(() -> orderService.removeItem(1L, 10L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order item does not belong to this order");
    }

    @Test
    void shouldNotRemoveItemWhenOrderIsConfirmed() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Product anotherProduct = new Product(
            "Keyboard",
            "Teclado mecânico",
            new BigDecimal("200.00")
        );

        Order order = new Order(user, List.of(
            new OrderItem(product, 1),
            new OrderItem(anotherProduct, 1)
        ));

        order.confirm();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.removeItem(1L, 10L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldNotRemoveLastActiveOrderItem() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        OrderItem item = new OrderItem(product, 1);

        Order order = new Order(user, List.of(item));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.removeItem(1L, 10L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order must have at least one item");

        assertThat(item.isActive()).isTrue();
    }

    @Test
    void shouldNotRemoveInactiveOrderItem() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        Product product = new Product(
            "Mouse",
            "Mouse sem fio",
            new BigDecimal("100.00")
        );

        Product anotherProduct = new Product(
            "Keyboard",
            "Teclado mecânico",
            new BigDecimal("200.00")
        );

        OrderItem item = new OrderItem(product, 1);
        OrderItem remainingItem = new OrderItem(anotherProduct, 1);

        Order order = new Order(user, List.of(item, remainingItem));

        item.remove();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.removeItem(1L, 10L))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Inactive order items cannot be removed");
    }
}