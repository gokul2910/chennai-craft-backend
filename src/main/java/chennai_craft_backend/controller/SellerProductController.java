package chennai_craft_backend.controller;

import chennai_craft_backend.dto.ProductRequest;
import chennai_craft_backend.dto.ProductResponse;
import chennai_craft_backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(Authentication authentication,
                                                  @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(authentication.getName(), request));
    }

    @GetMapping
    public List<ProductResponse> myProducts(Authentication authentication) {
        return productService.myProducts(authentication.getName());
    }
}