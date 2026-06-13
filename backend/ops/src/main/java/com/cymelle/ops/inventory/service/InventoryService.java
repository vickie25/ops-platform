package com.cymelle.ops.inventory.service;

import com.cymelle.ops.common.exception.InsufficientStockException;
import com.cymelle.ops.common.exception.ResourceNotFoundException;
import com.cymelle.ops.inventory.dto.ItemResponse;
import com.cymelle.ops.inventory.entity.Item;
import com.cymelle.ops.inventory.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ItemRepository itemRepository;

    /**
     * Get all inventory items.
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        log.info("Fetching all inventory items");
        return itemRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get item by ID.
     */
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long itemId) {
        log.info("Fetching item with ID: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Item not found with ID: %d", itemId)));
        return mapToResponse(item);
    }

    /**
     * Get all low-stock items (below their configured threshold).
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> getLowStockItems() {
        log.info("Fetching low-stock items");
        return itemRepository.findLowStockItems().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all items below a custom threshold.
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsBelowThreshold(Integer threshold) {
        log.info("Fetching items below threshold: {}", threshold);
        return itemRepository.findItemsBelowThreshold(threshold).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deduct stock for an item. Throws InsufficientStockException if not enough stock.
     * Used during order creation.
     */
    @Transactional
    public void deductStock(Long itemId, Integer quantity) {
        log.info("Deducting {} units from item ID: {}", quantity, itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Item not found with ID: %d", itemId)));

        if (item.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    String.format(
                            "Insufficient stock for item: %s. Available: %d, Requested: %d",
                            item.getName(), item.getStockQuantity(), quantity));
        }

        item.setStockQuantity(item.getStockQuantity() - quantity);
        itemRepository.save(item);

        log.info("Successfully deducted stock. New quantity: {}", item.getStockQuantity());
    }

    /**
     * Restore stock for an item. Used during order cancellation.
     */
    @Transactional
    public void restoreStock(Long itemId, Integer quantity) {
        log.info("Restoring {} units to item ID: {}", quantity, itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Item not found with ID: %d", itemId)));

        item.setStockQuantity(item.getStockQuantity() + quantity);
        itemRepository.save(item);

        log.info("Successfully restored stock. New quantity: {}", item.getStockQuantity());
    }

    /**
     * Map Item entity to ItemResponse DTO.
     */
    private ItemResponse mapToResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity())
                .lowStockThreshold(item.getLowStockThreshold())
                .isLowStock(item.isLowStock())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

}
