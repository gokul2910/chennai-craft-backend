package chennai_craft_backend.repository;

import chennai_craft_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    @Query("select p from Product p where p.stock > 0 "
            + "and lower(p.name) like lower(concat('%', :q, '%')) "
            + "order by p.createdAt desc")
    List<Product> searchInStock(@Param("q") String q);
}