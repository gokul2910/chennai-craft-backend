package chennai_craft_backend.service;

import chennai_craft_backend.dto.AdminSellerResponse;
import chennai_craft_backend.model.Role;
import chennai_craft_backend.model.SellerProfile;
import chennai_craft_backend.model.SellerStatus;
import chennai_craft_backend.model.User;
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
public class AdminSellerService {

    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;

    public List<AdminSellerResponse> list(SellerStatus status) {
        return sellerProfileRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminSellerResponse approve(Long id) {
        SellerProfile profile = findPending(id);
        profile.setStatus(SellerStatus.APPROVED);

        User user = profile.getUser();
        user.setRole(Role.SELLER);
        userRepository.save(user);

        return toResponse(sellerProfileRepository.save(profile));
    }

    @Transactional
    public AdminSellerResponse reject(Long id) {
        SellerProfile profile = findPending(id);
        profile.setStatus(SellerStatus.REJECTED);
        return toResponse(sellerProfileRepository.save(profile));
    }

    private SellerProfile findPending(Long id) {
        SellerProfile profile = sellerProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller application not found"));

        if (profile.getStatus() != SellerStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application is already " + profile.getStatus());
        }
        return profile;
    }

    private AdminSellerResponse toResponse(SellerProfile p) {
        User u = p.getUser();
        return new AdminSellerResponse(p.getId(), p.getShopName(), p.getPhone(),
                p.getArea(), p.getPincode(), p.getStatus(),
                u.getId(), u.getName(), u.getEmail());
    }
}