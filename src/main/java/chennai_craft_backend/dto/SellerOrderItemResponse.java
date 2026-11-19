package chennai_craft_backend.dto;

import chennai_craft_backend.model.OrderItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SellerOrderItemResponse(Long itemId, Long orderId, LocalDateTime orderedAt,
                                      String productName, BigDecimal unitPrice, Integer quantity,
                                      OrderItemStatus status,
                                      String customerName, String phone,
                                      String address, String pincode) {
}