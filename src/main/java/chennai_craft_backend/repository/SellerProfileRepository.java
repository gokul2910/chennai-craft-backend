package chennai_craft_backend.repository;

import chennai_craft_backend.model.SellerProfile;
import chennai_craft_backend.model.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    Optional<SellerProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<SellerProfile> findByStatus(SellerStatus status);
}