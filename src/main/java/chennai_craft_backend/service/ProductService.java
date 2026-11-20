package chennai_craft_backend.service;

import chennai_craft_backend.dto.ProductRequest;
import chennai_craft_backend.dto.ProductResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductResponse create(String email, ProductRequest request) {
        SellerProfile seller = getApprovedSeller(email);

        Product product = new Product();
        product.setSeller(seller);
        applyRequest(product, request);

        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> myProducts(String email) {
        SellerProfile seller = getApprovedSeller(email);
        return productRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse update(String email, Long id, ProductRequest request) {
        SellerProfile seller = getApprovedSeller(email);
        Product product = findOwnProduct(seller, id);

        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(String email, Long id) {
        SellerProfile seller = getApprovedSeller(email);
        Product product = findOwnProduct(seller, id);

        if (orderItemRepository.existsByProductId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This product has orders and cannot be deleted. Set its stock to 0 instead.");
        }
        productRepository.delete(product);
    }

    public List<ProductResponse> listPublic(String q) {
        String term = (q == null) ? "" : q.trim();
        return productRepository.searchInStock(term).stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getPublic(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return toResponse(product);
    }

    private Product findOwnProduct(SellerProfile seller, Long id) {
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // another seller's product? pretend it does not exist
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return product;
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

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName().trim());
        product.setDescription(blankToNull(request.getDescription()));
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(blankToNull(request.getImageUrl()));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(),
                p.getPrice(), p.getStock(), p.getImageUrl(),
                p.getSeller().getId(), p.getSeller().getShopName());
    }
}