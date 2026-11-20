package chennai_craft_backend.service;

import chennai_craft_backend.dto.SellerOrderItemResponse;
import chennai_craft_backend.model.CustomerOrder;
import chennai_craft_backend.model.OrderItem;
import chennai_craft_backend.model.OrderItemStatus;
import chennai_craft_backend.model.Product;
import chennai_craft_backend.model.Role;
import chennai_craft_backend.model.SellerProfile;
import chennai_craft_backend.model.SellerStatus;
import chennai_craft_backend.model.User;
import chennai_craft_backend.repository.OrderItemRepository;
import chennai_craft_backend.repository.ProductRepository;
import chennai_craft_backend.repository.SellerProfileRepository;
import chennai_craft_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerOrderServiceTest {

    private static final String SELLER_EMAIL = "seller@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private SellerProfileRepository sellerProfileRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SellerOrderService sellerOrderService;

    private final User sellerUser = user(2L, SELLER_EMAIL);
    private final SellerProfile shop = sellerProfile(10L, sellerUser);
    private final User buyer = user(1L, "buyer@example.com");

    @Test
    void cancel_putsStockBack() {
        givenApprovedSeller();
        Product product = product(1L, shop, 8);
        OrderItem item = orderItem(5L, shop, product, 2, OrderItemStatus.PLACED);
        when(orderItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        SellerOrderItemResponse response =
                sellerOrderService.updateStatus(SELLER_EMAIL, 5L, OrderItemStatus.CANCELLED);

        assertThat(response.status()).isEqualTo(OrderItemStatus.CANCELLED);
        assertThat(product.getStock()).isEqualTo(10);
    }

    @Test
    void confirmedToDelivered_isNotAllowed() {
        givenApprovedSeller();
        Product product = product(1L, shop, 8);
        OrderItem item = orderItem(5L, shop, product, 2, OrderItemStatus.CONFIRMED);
        when(orderItemRepository.findById(5L)).thenReturn(Optional.of(item));

        Throwable thrown = catchThrowable(
                () -> sellerOrderService.updateStatus(SELLER_EMAIL, 5L, OrderItemStatus.DELIVERED));

        assertStatus(thrown, HttpStatus.CONFLICT);
        assertThat(item.getStatus()).isEqualTo(OrderItemStatus.CONFIRMED);
        verifyNoInteractions(productRepository);
    }

    @Test
    void placedToConfirmed_isAllowed() {
        givenApprovedSeller();
        Product product = product(1L, shop, 8);
        OrderItem item = orderItem(5L, shop, product, 2, OrderItemStatus.PLACED);
        when(orderItemRepository.findById(5L)).thenReturn(Optional.of(item));

        SellerOrderItemResponse response =
                sellerOrderService.updateStatus(SELLER_EMAIL, 5L, OrderItemStatus.CONFIRMED);

        assertThat(response.status()).isEqualTo(OrderItemStatus.CONFIRMED);
        assertThat(product.getStock()).isEqualTo(8);
    }

    @Test
    void anotherSellersItem_isNotFound() {
        givenApprovedSeller();
        SellerProfile otherShop = sellerProfile(99L, user(3L, "other@example.com"));
        Product product = product(1L, otherShop, 8);
        OrderItem item = orderItem(5L, otherShop, product, 2, OrderItemStatus.PLACED);
        when(orderItemRepository.findById(5L)).thenReturn(Optional.of(item));

        Throwable thrown = catchThrowable(
                () -> sellerOrderService.updateStatus(SELLER_EMAIL, 5L, OrderItemStatus.CONFIRMED));

        assertStatus(thrown, HttpStatus.NOT_FOUND);
        assertThat(item.getStatus()).isEqualTo(OrderItemStatus.PLACED);
    }

    // ---------- helpers ----------

    private void givenApprovedSeller() {
        when(userRepository.findByEmail(SELLER_EMAIL)).thenReturn(Optional.of(sellerUser));
        when(sellerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(shop));
    }

    private void assertStatus(Throwable thrown, HttpStatus expected) {
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) thrown).getStatusCode()).isEqualTo(expected);
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail(email);
        user.setPassword("not-used");
        user.setRole(Role.CUSTOMER);
        return user;
    }

    private SellerProfile sellerProfile(Long id, User owner) {
        SellerProfile profile = new SellerProfile();
        profile.setId(id);
        profile.setUser(owner);
        profile.setShopName("Kala Crafts");
        profile.setStatus(SellerStatus.APPROVED);
        return profile;
    }

    private Product product(Long id, SellerProfile seller, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setSeller(seller);
        product.setName("Coaster Set");
        product.setPrice(new BigDecimal("349.00"));
        product.setStock(stock);
        return product;
    }

    private OrderItem orderItem(Long id, SellerProfile seller, Product product,
                                int quantity, OrderItemStatus status) {
        CustomerOrder order = new CustomerOrder();
        order.setId(100L);
        order.setCustomer(buyer);
        order.setAddress("12, Usman Road, T. Nagar, Chennai");
        order.setPhone("9876543210");
        order.setPincode("600017");

        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrder(order);
        item.setProduct(product);
        item.setSeller(seller);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setStatus(status);
        return item;
    }
}