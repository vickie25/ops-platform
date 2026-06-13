package com.cymelle.ops.order.controller;

import com.cymelle.ops.order.dto.CreateOrderRequest;
import com.cymelle.ops.order.dto.OrderResponse;
import com.cymelle.ops.order.entity.OrderStatus;
import com.cymelle.ops.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Endpoints for managing orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order with stock deduction.
     * POST /orders
     */
    @PostMapping
    @Operation(summary = "Create order", description = "Place a new order with stock validation and deduction")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /orders - Creating new order");
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all orders.
     * GET /orders
     */
    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve all orders sorted by newest first")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("GET /orders - Fetching all orders");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * Get order by ID.
     * GET /orders/{id}
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        log.info("GET /orders/{} - Fetching order", orderId);
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    /**
     * Get orders filtered by status.
     * GET /orders/filter/status?status=PENDING
     */
    @GetMapping("/filter/status")
    @Operation(summary = "Get orders by status", description = "Retrieve orders filtered by their status")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @RequestParam OrderStatus status) {
        log.info("GET /orders/filter/status?status={} - Fetching orders", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    /**
     * Get orders filtered by date range.
     * GET /orders/filter/date-range?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
     */
    @GetMapping("/filter/date-range")
    @Operation(summary = "Get orders by date range", description = "Retrieve orders within a date range")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("GET /orders/filter/date-range?from={}&to={} - Fetching orders", from, to);
        return ResponseEntity.ok(orderService.getOrdersByDateRange(from, to));
    }

    /**
     * Get orders filtered by status and date range.
     * GET /orders/filter/advanced?status=PENDING&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
     */
    @GetMapping("/filter/advanced")
    @Operation(summary = "Get orders by status and date range", description = "Retrieve orders with combined filters")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatusAndDateRange(
            @RequestParam OrderStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("GET /orders/filter/advanced?status={}&from={}&to={} - Fetching orders", status, from, to);
        return ResponseEntity.ok(orderService.getOrdersByStatusAndDateRange(status, from, to));
    }

    /**
     * Cancel an order and restore stock.
     * DELETE /orders/{id}
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel order", description = "Cancel an order and restore its stock to inventory")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        log.info("DELETE /orders/{} - Cancelling order", orderId);
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

}
