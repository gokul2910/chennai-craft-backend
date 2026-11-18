package chennai_craft_backend.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, String description,
                              BigDecimal price, Integer stock, String imageUrl,
                              Long sellerId, String shopName) {
}