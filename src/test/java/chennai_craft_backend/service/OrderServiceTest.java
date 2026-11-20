package chennai_craft_backend.service;

import chennai_craft_backend.dto.OrderLineRequest;
import chennai_craft_backend.dto.OrderResponse;
import chennai_craft_backend.dto.PlaceOrderRequest;
import chennai_craft_backend.model.CustomerOrder;
import chennai_craft_backend.model.OrderItemStatus;
import chennai_craft_backend.model.Product;
import chennai_craft_backend.model.Role;
import chennai_craft_backend.model.SellerProfile;
import chennai_craft_backend.model.SellerStatus;
import chennai_craft_backend.model.User;
import chennai_craft_backend.repository.CustomerOrderRepository;
import chennai_craft_backend.repository.ProductRepository;
import chennai_craft_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String CUSTOMER_EMAIL = "buyer@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerOrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private final User customer = user(1L, CUSTOMER_EMAIL);
    private final SellerProfile shop = sellerProfile(10L, user(2L, "seller@example.com"));

    @Test
    void placeOrder_reducesStockAndCalculatesTotal() {
        when(userRepository.findByEmail(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        Product coaster = product(1L, shop, "349.00", 10);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(coaster));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder(CUSTOMER_EMAIL, request(line(1L, 2)));

        assertThat(response.total()).isEqualByComparingTo("698.00");
        assertThat(coaster.getStock()).isEqualTo(8);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).status()).isEqualTo(OrderItemStatus.PLACED);
    }

    @Test
    void placeOrder_notEnoughStock_isRejectedAndNothingIsSaved() {
        when(userRepository.findByEmail(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        Product coaster = product(1L, shop, "349.00", 1);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(coaster));

        Throwable thrown = catchThrowable(
                () -> orderService.placeOrder(CUSTOMER_EMAIL, request(line(1L, 2))));

        assertStatus(thrown, HttpStatus.CONFLICT);
        assertThat(coaster.getStock()).isEqualTo(1);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_ownProduct_isRejected() {
        when(userRepository.findByEmail(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        SellerProfile customersOwnShop = sellerProfile(11L, customer);
        Product ownProduct = product(1L, customersOwnShop, "349.00", 10);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ownProduct));

        Throwable thrown = catchThrowable(
                () -> orderService.placeOrder(CUSTOMER_EMAIL, request(line(1L, 1))));

        assertStatus(thrown, HttpStatus.BAD_REQUEST);
        assertThat(ownProduct.getStock()).isEqualTo(10);
    }

    @Test
    void placeOrder_unknownProduct_isNotFound() {
        when(userRepository.findByEmail(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(
                () -> orderService.placeOrder(CUSTOMER_EMAIL, request(line(99L, 1))));

        assertStatus(thrown, HttpStatus.NOT_FOUND);
    }

    @Test
    void placeOrder_sameProductTwice_addsQuantitiesTogether() {
        when(userRepository.findByEmail(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        Product coaster = product(1L, shop, "349.00", 10);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(coaster));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder(CUSTOMER_EMAIL,
                request(line(1L, 2), line(1L, 3)));

        verify(productRepository, times(1)).findByIdForUpdate(1L);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
        assertThat(response.total()).isEqualByComparingTo("1745.00");
        assertThat(coaster.getStock()).isEqualTo(5);
    }

    // ---------- helpers ----------

    private void assertStatus(Throwable thrown, HttpStatus expected) {
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) thrown).getStatusCode()).isEqualTo(expected);
    }

    private PlaceOrderRequest request(OrderLineRequest... lines) {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setAddress("12, Usman Road, T. Nagar, Chennai");
        request.setPhone("9876543210");
        request.setPincode("600017");
        request.setItems(List.of(lines));
        return request;
    }

    private OrderLineRequest line(Long productId, int quantity) {
        OrderLineRequest line = new OrderLineRequest();
        line.setProductId(productId);
        line.setQuantity(quantity);
        return line;
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

    private Product product(Long id, SellerProfile seller, String price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setSeller(seller);
        product.setName("Coaster Set");
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        return product;
    }
}