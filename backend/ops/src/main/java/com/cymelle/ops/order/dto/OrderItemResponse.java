package com.cymelle.ops.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;

    private Long productId;

    private String productName;

    private Double unitPriceAtOrder;

    private Integer quantity;

    private Double subtotal;

}
