package com.caio.ordermanagement.order;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.caio.ordermanagement.order.dto.CreateOrderRequest;
import com.caio.ordermanagement.order.dto.CreateOrderResponse;
import com.caio.ordermanagement.order.dto.GetOrderResponse;
import com.caio.ordermanagement.order.exceptions.InvalidOrderException;
import com.caio.ordermanagement.order.exceptions.OrderNotFoundException;
import com.caio.ordermanagement.order.exceptions.OrderProductNotFoundException;
import com.caio.ordermanagement.order.exceptions.OrderUserNotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean 
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest(
            1L,
            List.of(
                new CreateOrderRequest.ItemRequest(10L, 2),
                new CreateOrderRequest.ItemRequest(20L, 1)
            )
        );

        CreateOrderResponse response = new CreateOrderResponse(

            100L,
            1L,
            OrderStatus.CREATED,
            new BigDecimal("7150.00"),
            Instant.parse("2026-08-24T12:00:00Z"),
            List.of(
                new CreateOrderResponse.ItemResponse(
                    10L,
                    2,
                    new BigDecimal("3500.00"),
                    new BigDecimal("7000.00")
                ),
                new CreateOrderResponse.ItemResponse(
                    20L,
                    1,
                    new BigDecimal("150.00"),
                    new BigDecimal("150.00")
                )
            )
        );

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.total").value(7150.00))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].productId").value(10))
            .andExpect(jsonPath("$.items[0].quantity").value(2))
            .andExpect(jsonPath("$.items[0].unitPrice").value(3500.00))
            .andExpect(jsonPath("$.items[0].subtotal").value(7000.00))
            .andExpect(jsonPath("$.items[1].productId").value(20))
            .andExpect(jsonPath("$.items[1].quantity").value(1))
            .andExpect(jsonPath("$.items[1].unitPrice").value(150.00))
            .andExpect(jsonPath("$.items[1].subtotal").value(150.00));
    }

    @Test
    void shouldRejectOrderWhenUserIdIsNull() throws Exception {

        String request = """
            {
                "userId": null,
                "items": [
                    {
                        "productId": 10,
                        "quantity": 1
                    }
                ]
            }
            """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderWhenItemsAreEmpty() throws Exception {

        String request = """
            {
                "userId": 1,
                "items": []
            }
            """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderWhenProductIdIsNull() throws Exception {

        String request = """
            {
                "userId": 1,
                "items": [
                    {
                        "productId": null,
                        "quantity": 1
                    }
                ]
            }
            """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderWhenQuantityIsZero() throws Exception {

        String request = """
            {
                "userId": 1,
                "items": [
                    {
                        "productId": 10,
                        "quantity": 0
                    }
                ]
            }
            """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderWhenQuantityIsNegative() throws Exception {

        String request = """
            {
                "userId": 1,
                "items": [
                    {
                        "productId": 10,
                        "quantity": -1
                    }
                ]
            }
            """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderWhenItemsAreNull() throws Exception {

        String request = """
            {
                "userId": 1,
                "items": null
            }
            """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {

        String request = """
            {
                "userId": 999,
                "items": [
                    {
                        "productId": 10,
                        "quantity": 1
                    }
                ]
            }
            """;

        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenThrow(new OrderUserNotFoundException(999L));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("User not found"))
            .andExpect(jsonPath("$.detail").value("User not found with id: 999"));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        String request = """
            {
                "userId": 1,
                "items": [
                    {
                        "productId": 999,
                        "quantity": 1
                    }
                ]
            }
            """;

        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenThrow(new OrderProductNotFoundException(999L));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Product not found"))
            .andExpect(jsonPath("$.detail").value("Product not found with id: 999"));
    }

    @Test
    void shouldReturnBadRequestWhenOrderIsInvalid() throws Exception {

        String request = """
            {
                "userId": 1,
                "items": [
                    {
                        "productId": 10,
                        "quantity": 1
                    }
                ]
            }
            """;

        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenThrow(new InvalidOrderException("Only active users can create orders"));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Invalid order"))
            .andExpect(jsonPath("$.detail")
            .value("Only active users can create orders"));
    }

    @Test
    void shouldGetOrder() throws Exception {

        GetOrderResponse response = new GetOrderResponse(

            100L,
            1L,
            OrderStatus.CREATED,
            new BigDecimal("7150.00"),
            Instant.parse("2026-08-24T12:00:00Z"),
            List.of(
                new GetOrderResponse.ItemResponse(
                    10L,
                    2,
                    new BigDecimal("3500.00"),
                    new BigDecimal("7000.00"),
                    true
                ),
                new GetOrderResponse.ItemResponse(
                    20L,
                    1,
                    new BigDecimal("150.00"),
                    new BigDecimal("150.00"),
                    true
                )
            )
        );

        when(orderService.getOrder(100L)).thenReturn(response);

        mockMvc.perform(get("/orders/100").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(100))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.total").value(7150.00))
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].productId").value(10))
            .andExpect(jsonPath("$.items[0].quantity").value(2))
            .andExpect(jsonPath("$.items[0].unitPrice").value(3500.00))
            .andExpect(jsonPath("$.items[0].subtotal").value(7000.00))
            .andExpect(jsonPath("$.items[0].active").value(true))
            .andExpect(jsonPath("$.items[1].productId").value(20))
            .andExpect(jsonPath("$.items[1].quantity").value(1))
            .andExpect(jsonPath("$.items[1].unitPrice").value(150.00))
            .andExpect(jsonPath("$.items[1].subtotal").value(150.00))
            .andExpect(jsonPath("$.items[1].active").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {

        when(orderService.getOrder(999L)).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/orders/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Order not found"))
            .andExpect(jsonPath("$.detail")
            .value("Order not found with id: 999"));
    }
}