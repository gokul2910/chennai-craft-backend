package chennai_craft_backend.controller;

import chennai_craft_backend.dto.SellerApplyRequest;
import chennai_craft_backend.dto.SellerProfileResponse;
import chennai_craft_backend.service.SellerService;
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

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/apply")
    public ResponseEntity<SellerProfileResponse> apply(Authentication authentication,
                                                       @Valid @RequestBody SellerApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sellerService.apply(authentication.getName(), request));
    }

    @GetMapping("/application")
    public ResponseEntity<SellerProfileResponse> myApplication(Authentication authentication) {
        return ResponseEntity.ok(sellerService.getMyApplication(authentication.getName()));
    }
}