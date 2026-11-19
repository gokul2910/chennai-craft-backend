package chennai_craft_backend.controller;

import chennai_craft_backend.dto.SellerOrderItemResponse;
import chennai_craft_backend.dto.UpdateStatusRequest;
import chennai_craft_backend.service.SellerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    @PatchMapping("/{itemId}/status")
    public SellerOrderItemResponse updateStatus(Authentication authentication,
                                                @PathVariable Long itemId,
                                                @Valid @RequestBody UpdateStatusRequest request) {
        return sellerOrderService.updateStatus(authentication.getName(), itemId, request.getStatus());
    }
}