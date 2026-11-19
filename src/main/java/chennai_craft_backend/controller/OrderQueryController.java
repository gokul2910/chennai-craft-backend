package chennai_craft_backend.controller;

import chennai_craft_backend.dto.OrderResponse;
import chennai_craft_backend.dto.SellerOrderItemResponse;
import chennai_craft_backend.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    @GetMapping("/orders/my")
    public List<OrderResponse> myOrders(Authentication authentication) {
        return orderQueryService.myOrders(authentication.getName());
    }

    @GetMapping("/seller/orders")
    public List<SellerOrderItemResponse> sellerOrders(Authentication authentication) {
        return orderQueryService.sellerOrders(authentication.getName());
    }
}