package com.cymelle.ops.order.service;

import com.cymelle.ops.common.exception.InsufficientStockException;
import com.cymelle.ops.common.exception.InvalidOrderStateException;
import com.cymelle.ops.common.exception.ResourceNotFoundException;
import com.cymelle.ops.inventory.entity.Item;
import com.cymelle.ops.inventory.repository.ItemRepository;
import com.cymelle.ops.inventory.service.InventoryService;
import com.cymelle.ops.order.dto.CreateOrderRequest;
import com.cymelle.ops.order.dto.OrderItemRequest;
import com.cymelle.ops.order.dto.OrderResponse;
import com.cymelle.ops.order.entity.Order;
import com.cymelle.ops.order.entity.OrderItem;
import com.cymelle.ops.order.entity.OrderStatus;
import com.cymelle.ops.order.repository.OrderItemRepository;
import com.cymelle.ops.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = Item.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(100.0)
                .stockQuantity(50)
                .lowStockThreshold(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should deduct stock when order is created")
    void shouldDeductStockWhenOrderCreated() {
        // Given: A valid order request with sufficient stock
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(1L)
                .quantity(5)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(Arrays.asList(itemRequest))
                .build();

        Order savedOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(500.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(OrderItem.builder().build());

        // When: Create order
        OrderResponse response = orderService.createOrder(request);

        // Then: Stock should be deducted
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(inventoryService, times(1)).deductStock(1L, 5);
    }

    @Test
    @DisplayName("Should throw exception when stock is insufficient")
    void shouldThrowWhenStockInsufficient() {
        // Given: Order request with quantity exceeding available stock
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(1L)
                .quantity(100) // More than available (50)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(Arrays.asList(itemRequest))
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When & Then: Should throw InsufficientStockException
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should restore stock when order is cancelled")
    void shouldRestoreStockWhenOrderCancelled() {
        // Given: An existing confirmed order
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .productId(1L)
                .productName("Test Product")
                .unitPriceAtOrder(100.0)
                .quantity(5)
                .subtotal(500.0)
                .build();

        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CONFIRMED)
                .items(Arrays.asList(orderItem))
                .totalAmount(500.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When: Cancel order
        OrderResponse response = orderService.cancelOrder(1L);

        // Then: Stock should be restored
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(inventoryService, times(1)).restoreStock(1L, 5);
    }

    @Test
    @DisplayName("Should throw exception when trying to cancel non-existent order")
    void shouldThrowWhenCancellingNonExistentOrder() {
        // Given: Non-existent order ID
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then: Should throw ResourceNotFoundException
        assertThatThrownBy(() -> orderService.cancelOrder(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should throw exception when trying to cancel already cancelled order")
    void shouldThrowWhenCancellingAlreadyCancelledOrder() {
        // Given: An already cancelled order
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .items(Arrays.asList())
                .totalAmount(500.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then: Should throw InvalidOrderStateException
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    @DisplayName("Should throw exception when trying to cancel completed order")
    void shouldThrowWhenCancellingCompletedOrder() {
        // Given: A completed order
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.COMPLETED)
                .items(Arrays.asList())
                .totalAmount(500.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then: Should throw InvalidOrderStateException
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("Cannot cancel completed order");
    }

    @Test
    @DisplayName("Should retrieve order by ID")
    void shouldRetrieveOrderById() {
        // Given: An existing order
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CONFIRMED)
                .items(Arrays.asList())
                .totalAmount(500.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When: Get order by ID
        OrderResponse response = orderService.getOrderById(1L);

        // Then: Should return the order
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should throw exception when order ID not found")
    void shouldThrowWhenOrderIdNotFound() {
        // Given: Non-existent order ID
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then: Should throw ResourceNotFoundException
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should retrieve all orders")
    void shouldRetrieveAllOrders() {
        // Given: Multiple orders
        Order order1 = Order.builder()
                .id(1L)
                .status(OrderStatus.CONFIRMED)
                .items(Arrays.asList())
                .totalAmount(500.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order order2 = Order.builder()
                .id(2L)
                .status(OrderStatus.PENDING)
                .items(Arrays.asList())
                .totalAmount(300.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(order1, order2));

        // When: Get all orders
        var responses = orderService.getAllOrders();

        // Then: Should return all orders
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("Should retrieve orders by status")
    void shouldRetrieveOrdersByStatus() {
        // Given: Orders with specific status
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CONFIRMED)
                .items(Arrays.asList())
                .totalAmount(500.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByStatus(OrderStatus.CONFIRMED))
                .thenReturn(Arrays.asList(order));

        // When: Get orders by status
        var responses = orderService.getOrdersByStatus(OrderStatus.CONFIRMED);

        // Then: Should return filtered orders
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should handle order with multiple items correctly")
    void shouldHandleOrderWithMultipleItems() {
        // Given: Order request with multiple items
        Item item1 = Item.builder()
                .id(1L)
                .name("Product 1")
                .price(50.0)
                .stockQuantity(100)
                .lowStockThreshold(5)
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("Product 2")
                .price(75.0)
                .stockQuantity(100)
                .lowStockThreshold(5)
                .build();

        OrderItemRequest itemRequest1 = OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        OrderItemRequest itemRequest2 = OrderItemRequest.builder()
                .productId(2L)
                .quantity(3)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(Arrays.asList(itemRequest1, itemRequest2))
                .build();

        Order savedOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(325.0) // (50*2) + (75*3) = 100 + 225
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(OrderItem.builder().build());

        // When: Create order
        OrderResponse response = orderService.createOrder(request);

        // Then: Total amount should be correct
        assertThat(response.getTotalAmount()).isEqualTo(325.0);
        verify(inventoryService, times(1)).deductStock(1L, 2);
        verify(inventoryService, times(1)).deductStock(2L, 3);
    }

}
