package com.cymelle.ops.inventory.controller;

import com.cymelle.ops.inventory.dto.ItemResponse;
import com.cymelle.ops.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "Endpoints for managing inventory and stock levels")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Get all inventory items.
     */
    @GetMapping
    @Operation(summary = "Get all items", description = "Retrieve all inventory items with their current stock levels")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        log.info("GET /inventory - Fetching all items");
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    /**
     * Get item by ID.
     */
    @GetMapping("/{itemId}")
    @Operation(summary = "Get item by ID", description = "Retrieve a specific inventory item by its ID")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long itemId) {
        log.info("GET /inventory/{} - Fetching item", itemId);
        return ResponseEntity.ok(inventoryService.getItemById(itemId));
    }

    /**
     * Get all low-stock items.
     * Uses the item's configured low-stock threshold.
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get low-stock items", description = "Retrieve all items with stock below their configured threshold")
    public ResponseEntity<List<ItemResponse>> getLowStockItems() {
        log.info("GET /inventory/low-stock - Fetching low-stock items");
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    /**
     * Get items below a custom threshold.
     * Allows overriding the item's configured threshold via query parameter.
     */
    @GetMapping("/threshold")
    @Operation(summary = "Get items below threshold", description = "Retrieve items with stock below a custom threshold")
    public ResponseEntity<List<ItemResponse>> getItemsBelowThreshold(
            @RequestParam(defaultValue = "5") Integer threshold) {
        log.info("GET /inventory/threshold?threshold={} - Fetching items", threshold);
        return ResponseEntity.ok(inventoryService.getItemsBelowThreshold(threshold));
    }

}
