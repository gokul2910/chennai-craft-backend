package chennai_craft_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long id, BigDecimal total, String address, String phone,
                            String pincode, LocalDateTime createdAt,
                            List<OrderItemResponse> items) {
}