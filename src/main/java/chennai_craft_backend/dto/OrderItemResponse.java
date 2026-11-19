package chennai_craft_backend.dto;

import chennai_craft_backend.model.OrderItemStatus;

import java.math.BigDecimal;

public record OrderItemResponse(Long id, Long productId, String productName,
                                BigDecimal unitPrice, Integer quantity,
                                Long sellerId, String shopName, OrderItemStatus status) {
}