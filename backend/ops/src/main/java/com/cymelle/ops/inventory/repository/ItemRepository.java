package com.cymelle.ops.inventory.repository;

import com.cymelle.ops.inventory.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find all items with stock below their configured threshold.
     */
    @Query("SELECT i FROM Item i WHERE i.stockQuantity < i.lowStockThreshold ORDER BY i.stockQuantity ASC")
    List<Item> findLowStockItems();

    /**
     * Find all items with stock below a custom threshold.
     */
    @Query("SELECT i FROM Item i WHERE i.stockQuantity < :threshold ORDER BY i.stockQuantity ASC")
    List<Item> findItemsBelowThreshold(@Param("threshold") Integer threshold);

    /**
     * Find items by name (case-insensitive).
     */
    List<Item> findByNameIgnoreCaseContaining(String name);

}
