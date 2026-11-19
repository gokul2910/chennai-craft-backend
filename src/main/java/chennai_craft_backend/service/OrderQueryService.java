package chennai_craft_backend.service;

import chennai_craft_backend.dto.OrderItemResponse;
import chennai_craft_backend.dto.OrderResponse;
import chennai_craft_backend.dto.SellerOrderItemResponse;
import chennai_craft_backend.model.CustomerOrder;
import chennai_craft_backend.model.OrderItem;
import chennai_craft_backend.model.SellerProfile;
import chennai_craft_backend.model.SellerStatus;
import chennai_craft_backend.model.User;
import chennai_craft_backend.repository.CustomerOrderRepository;
import chennai_craft_backend.repository.OrderItemRepository;
import chennai_craft_backend.repository.SellerProfileRepository;
import chennai_craft_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(String email) {
        User user = findUser(email);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SellerOrderItemResponse> sellerOrders(String email) {
        User user = findUser(email);

        SellerProfile seller = sellerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a seller"));
        if (seller.getStatus() != SellerStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller account is not approved");
        }

        return orderItemRepository.findBySellerIdOrderByIdDesc(seller.getId()).stream()
                .map(this::toSellerItem)
                .toList();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private OrderResponse toOrderResponse(CustomerOrder o) {
        return new OrderResponse(o.getId(), o.getTotal(), o.getAddress(), o.getPhone(),
                o.getPincode(), o.getCreatedAt(),
                o.getItems().stream().map(this::toItemResponse).toList());
    }

    private OrderItemResponse toItemResponse(OrderItem i) {
        return new OrderItemResponse(i.getId(), i.getProduct().getId(), i.getProductName(),
                i.getUnitPrice(), i.getQuantity(), i.getSeller().getId(),
                i.getSeller().getShopName(), i.getStatus());
    }

    private SellerOrderItemResponse toSellerItem(OrderItem i) {
        CustomerOrder o = i.getOrder();
        return new SellerOrderItemResponse(i.getId(), o.getId(), o.getCreatedAt(),
                i.getProductName(), i.getUnitPrice(), i.getQuantity(), i.getStatus(),
                o.getCustomer().getName(), o.getPhone(), o.getAddress(), o.getPincode());
    }
}