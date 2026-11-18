package chennai_craft_backend.service;

import chennai_craft_backend.dto.SellerApplyRequest;
import chennai_craft_backend.dto.SellerProfileResponse;
import chennai_craft_backend.model.SellerProfile;
import chennai_craft_backend.model.SellerStatus;
import chennai_craft_backend.model.User;
import chennai_craft_backend.repository.SellerProfileRepository;
import chennai_craft_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;

    public SellerProfileResponse apply(String email, SellerApplyRequest request) {
        User user = findUser(email);

        if (sellerProfileRepository.existsByUserId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied as a seller");
        }

        SellerProfile profile = new SellerProfile();
        profile.setUser(user);
        profile.setShopName(request.getShopName().trim());
        profile.setPhone(request.getPhone().trim());
        profile.setArea(request.getArea().trim());
        profile.setPincode(request.getPincode().trim());
        profile.setStatus(SellerStatus.PENDING);

        return toResponse(sellerProfileRepository.save(profile));
    }

    public SellerProfileResponse getMyApplication(String email) {
        User user = findUser(email);
        SellerProfile profile = sellerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No seller application found"));
        return toResponse(profile);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private SellerProfileResponse toResponse(SellerProfile p) {
        return new SellerProfileResponse(p.getId(), p.getShopName(), p.getPhone(),
                p.getArea(), p.getPincode(), p.getStatus());
    }
}