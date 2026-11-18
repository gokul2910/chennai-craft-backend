package chennai_craft_backend.dto;

import chennai_craft_backend.model.SellerStatus;

public record AdminSellerResponse(Long id, String shopName, String phone,
                                  String area, String pincode, SellerStatus status,
                                  Long userId, String userName, String userEmail) {
}