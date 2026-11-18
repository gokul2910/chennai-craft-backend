package chennai_craft_backend.controller;

import chennai_craft_backend.dto.AdminSellerResponse;
import chennai_craft_backend.model.SellerStatus;
import chennai_craft_backend.service.AdminSellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sellers")
@RequiredArgsConstructor
public class AdminSellerController {

    private final AdminSellerService adminSellerService;

    @GetMapping
    public List<AdminSellerResponse> list(@RequestParam(defaultValue = "PENDING") SellerStatus status) {
        return adminSellerService.list(status);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AdminSellerResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(adminSellerService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AdminSellerResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(adminSellerService.reject(id));
    }
}