package com.cymelle.ops.order.repository;

import com.cymelle.ops.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items for a specific order.
     */
    List<OrderItem> findByOrderId(Long orderId);

}
