package com.cymelle.ops.order.repository;

import com.cymelle.ops.order.entity.Order;
import com.cymelle.ops.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders with a specific status.
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders created between two dates.
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<Order> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders by status and date range.
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<Order> findByStatusAndDateRange(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find all orders ordered by creation date (newest first).
     */
    List<Order> findAllByOrderByCreatedAtDesc();

}
