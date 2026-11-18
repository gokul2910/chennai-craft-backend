package chennai_craft_backend.repository;

import chennai_craft_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}