package chennai_craft_backend.dto;

import chennai_craft_backend.model.SellerStatus;

public record SellerProfileResponse(Long id, String shopName, String phone,
                                    String area, String pincode, SellerStatus status) {
}