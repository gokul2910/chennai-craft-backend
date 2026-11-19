package chennai_craft_backend.service;

import chennai_craft_backend.dto.SellerOrderItemResponse;
import chennai_craft_backend.model.CustomerOrder;
import chennai_craft_backend.model.OrderItem;
import chennai_craft_backend.model.OrderItemStatus;
import chennai_craft_backend.model.Product;
import chennai_craft_backend.model.SellerProfile;
import chennai_craft_backend.model.SellerStatus;
import chennai_craft_backend.model.User;
import chennai_craft_backend.repository.OrderItemRepository;
import chennai_craft_backend.repository.ProductRepository;
import chennai_craft_backend.repository.SellerProfileRepository;
import chennai_craft_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public SellerOrderItemResponse updateStatus(String email, Long itemId, OrderItemStatus newStatus) {
        SellerProfile seller = getApprovedSeller(email);

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        // another seller's item? pretend it does not exist
        if (!item.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found");
        }

        OrderItemStatus current = item.getStatus();
        if (!isAllowed(current, newStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change status from " + current + " to " + newStatus);
        }

        if (newStatus == OrderItemStatus.CANCELLED) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
            product.setStock(product.getStock() + item.getQuantity());
        }

        item.setStatus(newStatus);
        return toResponse(item);
    }

    private boolean isAllowed(OrderItemStatus from, OrderItemStatus to) {
        return switch (from) {
            case PLACED -> to == OrderItemStatus.CONFIRMED || to == OrderItemStatus.CANCELLED;
            case CONFIRMED -> to == OrderItemStatus.SHIPPED || to == OrderItemStatus.CANCELLED;
            case SHIPPED -> to == OrderItemStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private SellerProfile getApprovedSeller(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        SellerProfile seller = sellerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a seller"));

        if (seller.getStatus() != SellerStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller account is not approved");
        }
        return seller;
    }

    private SellerOrderItemResponse toResponse(OrderItem i) {
        CustomerOrder o = i.getOrder();
        return new SellerOrderItemResponse(i.getId(), o.getId(), o.getCreatedAt(),
                i.getProductName(), i.getUnitPrice(), i.getQuantity(), i.getStatus(),
                o.getCustomer().getName(), o.getPhone(), o.getAddress(), o.getPincode());
    }
}