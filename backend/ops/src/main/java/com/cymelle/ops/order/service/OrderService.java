package com.cymelle.ops.order.service;

import com.cymelle.ops.common.exception.InvalidOrderStateException;
import com.cymelle.ops.common.exception.ResourceNotFoundException;
import com.cymelle.ops.inventory.entity.Item;
import com.cymelle.ops.inventory.repository.ItemRepository;
import com.cymelle.ops.inventory.service.InventoryService;
import com.cymelle.ops.order.dto.CreateOrderRequest;
import com.cymelle.ops.order.dto.OrderItemRequest;
import com.cymelle.ops.order.dto.OrderItemResponse;
import com.cymelle.ops.order.dto.OrderResponse;
import com.cymelle.ops.order.entity.Order;
import com.cymelle.ops.order.entity.OrderItem;
import com.cymelle.ops.order.entity.OrderStatus;
import com.cymelle.ops.order.repository.OrderRepository;
import com.cymelle.ops.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    /**
     * Create a new order with stock deduction (all in one transaction).
     * This is the critical flow for order placement.
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order with {} items", request.getItems().size());

        // Step 1: Validate all items exist and have sufficient stock
        Double totalAmount = 0.0;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Item item = itemRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Item not found with ID: %d", itemRequest.getProductId())));

            // Validate stock exists
            if (item.getStockQuantity() < itemRequest.getQuantity()) {
                throw new com.cymelle.ops.common.exception.InsufficientStockException(
                        String.format(
                                "Insufficient stock for item: %s. Available: %d, Requested: %d",
                                item.getName(), item.getStockQuantity(), itemRequest.getQuantity()));
            }

            // Prepare order item with price snapshot
            OrderItem orderItem = OrderItem.builder()
                    .productId(item.getId())
                    .productName(item.getName())
                    .unitPriceAtOrder(item.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .build();
            orderItem.calculateSubtotal();

            orderItems.add(orderItem);
            totalAmount += orderItem.getSubtotal();
        }

        // Step 2: Create order
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .items(orderItems)
                .build();
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        // Step 3: Deduct stock and associate order items
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(savedOrder);
            orderItemRepository.save(orderItem);
            inventoryService.deductStock(orderItem.getProductId(), orderItem.getQuantity());
            log.info("Stock deducted for product ID: {} (quantity: {})",
                    orderItem.getProductId(), orderItem.getQuantity());
        }

        // Step 4: Mark order as confirmed
        savedOrder.setStatus(OrderStatus.CONFIRMED);
        savedOrder = orderRepository.save(savedOrder);
        log.info("Order {} status updated to CONFIRMED", savedOrder.getId());

        return mapToResponse(savedOrder);
    }

    /**
     * Get order by ID.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Order not found with ID: %d", orderId)));
        return mapToResponse(order);
    }

    /**
     * Get all orders (newest first).
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by status.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by date range.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching orders between {} and {}", startDate, endDate);
        return orderRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by status and date range.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatusAndDateRange(
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        log.info("Fetching orders with status: {} between {} and {}", status, startDate, endDate);
        return orderRepository.findByStatusAndDateRange(status, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel an order and restore stock.
     * This is the critical flow for order cancellation.
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Order not found with ID: %d", orderId)));

        // Validate order can be cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    String.format("Order %d is already cancelled", orderId));
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderStateException(
                    String.format("Cannot cancel completed order %d", orderId));
        }

        // Restore stock for each order item
        for (OrderItem item : order.getItems()) {
            inventoryService.restoreStock(item.getProductId(), item.getQuantity());
            log.info("Stock restored for product ID: {} (quantity: {})",
                    item.getProductId(), item.getQuantity());
        }

        // Mark order as cancelled
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} marked as CANCELLED", updatedOrder.getId());

        return mapToResponse(updatedOrder);
    }

    /**
     * Map Order entity to OrderResponse DTO.
     */
    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .items(order.getItems().stream()
                        .map(this::mapOrderItemToResponse)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * Map OrderItem entity to OrderItemResponse DTO.
     */
    private OrderItemResponse mapOrderItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .unitPriceAtOrder(item.getUnitPriceAtOrder())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

}
