package chennai_craft_backend.service;

import chennai_craft_backend.dto.OrderItemResponse;
import chennai_craft_backend.dto.OrderLineRequest;
import chennai_craft_backend.dto.OrderResponse;
import chennai_craft_backend.dto.PlaceOrderRequest;
import chennai_craft_backend.model.CustomerOrder;
import chennai_craft_backend.model.OrderItem;
import chennai_craft_backend.model.OrderItemStatus;
import chennai_craft_backend.model.Product;
import chennai_craft_backend.model.User;
import chennai_craft_backend.repository.CustomerOrderRepository;
import chennai_craft_backend.repository.ProductRepository;
import chennai_craft_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;

    @Transactional
    public OrderResponse placeOrder(String email, PlaceOrderRequest request) {
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        // same product twice in the list? add the quantities together
        Map<Long, Integer> wanted = new TreeMap<>();
        for (OrderLineRequest line : request.getItems()) {
            wanted.merge(line.getProductId(), line.getQuantity(), Integer::sum);
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setAddress(request.getAddress().trim());
        order.setPhone(request.getPhone().trim());
        order.setPincode(request.getPincode().trim());

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : wanted.entrySet()) {
            Long productId = entry.getKey();
            int qty = entry.getValue();

            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product " + productId + " not found"));

            if (product.getSeller().getUser().getId().equals(customer.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "You cannot buy your own product");
            }
            if (product.getStock() < qty) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Not enough stock for " + product.getName());
            }

            product.setStock(product.getStock() - qty);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setSeller(product.getSeller());
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(qty);
            item.setStatus(OrderItemStatus.PLACED);
            order.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotal(total);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(CustomerOrder o) {
        return new OrderResponse(o.getId(), o.getTotal(), o.getAddress(), o.getPhone(),
                o.getPincode(), o.getCreatedAt(),
                o.getItems().stream().map(this::toItemResponse).toList());
    }

    private OrderItemResponse toItemResponse(OrderItem i) {
        return new OrderItemResponse(i.getId(), i.getProduct().getId(), i.getProductName(),
                i.getUnitPrice(), i.getQuantity(), i.getSeller().getId(),
                i.getSeller().getShopName(), i.getStatus());
    }
}