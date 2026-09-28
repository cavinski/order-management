package com.caio.ordermanagement.order;

import com.caio.ordermanagement.product.Product;
import com.caio.ordermanagement.user.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.caio.ordermanagement.order.exceptions.InvalidOrderException;
import com.caio.ordermanagement.order.exceptions.InvalidOrderItemException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {
    
     @Test
    void shouldCreateOrder() {

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

        OrderItem item = new OrderItem(product, 2);

        Order order = new Order(user, List.of(item));

        assertThat(order.getUser()).isSameAs(user);
        assertThat(order.getItems()).containsExactly(item);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldNotCreateOrderWithInactiveUser() {

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

        OrderItem item = new OrderItem(product, 2);

        assertThatThrownBy(() -> new Order(user, List.of(item)))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Only active users can create orders");
    }

    @Test
    void shouldNotCreateOrderWithoutItems() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        assertThatThrownBy(() -> new Order(user, List.of()))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order must have at least one item");
    }

    @Test
    void shouldNotCreateOrderWithNullItems() {

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        assertThatThrownBy(() -> new Order(user, null))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order must have at least one item");
    }

    @Test
    void shouldNotCreateOrderWithDuplicateProducts() {

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

        OrderItem firstItem = new OrderItem(product, 2);
        OrderItem secondItem = new OrderItem(product, 3);

        assertThatThrownBy(() -> new Order(user, List.of(firstItem, secondItem)))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order cannot contain the same product more than once");
    }

    @Test
    void shouldCalculateOrderTotal() {

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
        OrderItem mouseItem = new OrderItem(mouse, 3);

        Order order = new Order(user, List.of(notebookItem, mouseItem));

        assertThat(order.getTotal()).isEqualByComparingTo("7450.00");
    }

    @Test
    void shouldAddItemToCreatedOrder() {

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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 2);

        Order order = new Order(user, List.of(notebookItem));

        order.addItem(mouseItem);

        assertThat(order.getItems()).containsExactly(notebookItem, mouseItem);
    }

    @Test
    void shouldNotAddDuplicateProduct() {

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

        OrderItem firstItem = new OrderItem(notebook, 1);
        OrderItem secondItem = new OrderItem(notebook, 2);

        Order order = new Order(user, List.of(firstItem));

        assertThatThrownBy(() -> order.addItem(secondItem))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order cannot contain the same product more than once");
    }

    @Test
    void shouldDeactivateItemWhenRemovedFromCreatedOrder() {

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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 2);

        Order order = new Order(user, List.of(notebookItem, mouseItem));

        order.removeItem(mouseItem);

        assertThat(mouseItem.isActive()).isFalse();
        assertThat(order.getItems()).containsExactly(notebookItem, mouseItem);
    }

    @Test
    void shouldNotRemoveLastItem() {

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

        OrderItem item = new OrderItem(notebook, 1);

        Order order = new Order(user, List.of(item));

        assertThatThrownBy(() -> order.removeItem(item))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order must have at least one item");
    }

    @Test
    void shouldNotRemoveItemThatDoesNotBelongToOrder() {

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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 1);

        Order order = new Order(user, List.of(notebookItem));

        assertThatThrownBy(() -> order.removeItem(mouseItem))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order item does not belong to this order");
    }

    @Test
    void shouldUpdateItemQuantity() {

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

        OrderItem item = new OrderItem(notebook, 2);
        Order order = new Order(user, List.of(item));

        order.updateItemQuantity(item, 5);

        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(order.getTotal()).isEqualByComparingTo("17500.00");
    }

    @Test
    void shouldNotUpdateQuantityOfItemThatDoesNotBelongToOrder() {

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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 1);

        Order order = new Order(user, List.of(notebookItem));

        assertThatThrownBy(() -> order.updateItemQuantity(mouseItem, 5))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order item does not belong to this order");
    }

    @Test
    void shouldNotUpdateItemQuantityToZero() {

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

        OrderItem item = new OrderItem(notebook, 2);
        Order order = new Order(user, List.of(item));

        assertThatThrownBy(() -> order.updateItemQuantity(item, 0))
            .isInstanceOf(InvalidOrderItemException.class)
            .hasMessage("Quantity must be positive");
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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldNotConfirmOrderThatIsAlreadyConfirmed() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();

        assertThatThrownBy(order::confirm)
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be confirmed from CREATED status");
    }

    @Test
    void shouldStartProcessingConfirmedOrder() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void shouldNotUpdateItemAfterOrderIsConfirmed() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();

        assertThatThrownBy(() -> order.updateItemQuantity(item, 2))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldNotAddItemAfterOrderIsConfirmed() {

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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 1);

        Order order = new Order(user, List.of(notebookItem));

        order.confirm();

        assertThatThrownBy(() -> order.addItem(mouseItem))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldNotRemoveItemAfterOrderIsConfirmed() {

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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 1);

        Order order = new Order(user, List.of(notebookItem, mouseItem));

        order.confirm();

        assertThatThrownBy(() -> order.removeItem(mouseItem))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldShipProcessingOrder() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.ship();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void shouldNotShipConfirmedOrder() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();

        assertThatThrownBy(order::ship)
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be shipped from PROCESSING status");
    }

    @Test
    void shouldDeliverShippedOrder() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void shouldNotDeliverAlreadyDeliveredOrder() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        assertThatThrownBy(order::deliver)
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be delivered from SHIPPED status");
    }

    @Test
    void shouldNotStartProcessingDeliveredOrder() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        assertThatThrownBy(order::startProcessing)
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only start processing from CONFIRMED status");
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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.cancel();

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.ship();

        assertThatThrownBy(order::cancel)
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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        assertThatThrownBy(order::cancel)
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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.cancel();

        assertThatThrownBy(order::cancel)
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order can only be cancelled from CREATED, CONFIRMED or PROCESSING status");
    }

    @Test
    void shouldNotUpdateItemsWhenOrderIsProcessing() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();

        assertThatThrownBy(() -> order.updateItemQuantity(item, 2))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldNotUpdateItemsWhenOrderIsShipped() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.ship();

        assertThatThrownBy(() -> order.updateItemQuantity(item, 2))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldNotUpdateItemsWhenOrderIsDelivered() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        assertThatThrownBy(() -> order.updateItemQuantity(item, 2))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldNotUpdateItemsWhenOrderIsCancelled() {

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

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        order.cancel();

        assertThatThrownBy(() -> order.updateItemQuantity(item, 2))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Order items can only be changed while order is CREATED");
    }

    @Test
    void shouldSetCreatedAtWhenOrderIsCreated() {

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

        Instant before = Instant.now();

        OrderItem item = new OrderItem(product, 1);
        Order order = new Order(user, List.of(item));

        Instant after = Instant.now();

        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getCreatedAt()).isBetween(before, after);
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

        OrderItem item = new OrderItem(product, 1);

        assertThatThrownBy(() -> new Order(user, List.of(item)))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Inactive products cannot be added to orders");
    }

    @Test
    void shouldNotAddInactiveProductToCreatedOrder() {

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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 1);

        Order order = new Order(user, List.of(notebookItem));

        mouse.deactivate();

        assertThatThrownBy(() -> order.addItem(mouseItem))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Inactive products cannot be added to orders");
    }

    @Test
    void shouldNotIncludeInactiveItemInOrderTotal() {

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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 2);

        Order order = new Order(user, List.of(notebookItem, mouseItem));

        order.removeItem(mouseItem);

        assertThat(order.getTotal()).isEqualByComparingTo("3500.00");
    }

    @Test
    void shouldNotUpdateInactiveItem() {
        
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

        OrderItem notebookItem = new OrderItem(notebook, 1);
        OrderItem mouseItem = new OrderItem(mouse, 1);

        Order order = new Order(user, List.of(notebookItem, mouseItem));

        order.removeItem(mouseItem);

        assertThatThrownBy(() -> order.updateItemQuantity(mouseItem, 5))
            .isInstanceOf(InvalidOrderException.class)
            .hasMessage("Inactive order items cannot be updated");
    }
}