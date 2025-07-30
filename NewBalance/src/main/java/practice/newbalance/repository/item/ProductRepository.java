package practice.newbalance.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import practice.newbalance.domain.item.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("select p from Product p LEFT JOIN FETCH p.category")
    List<Product> findAllProducts();


    @Query("select p from Product p LEFT JOIN FETCH p.productOptions po LEFT JOIN FETCH p.category WHERE p.id = :productId")
    List<Product> findProductWithProductOptionsById(@Param("productId") Long productId);

    @Query("select p from Product p join fetch p.category join fetch p.productOptions where p.id = :productId")
    Optional<Product> findProductById(@Param("productId") Long productId);

    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT DISTINCT p.size FROM ProductOption p WHERE p.product.id = :productId")
    List<String> findSizeValuesByProductId(@Param("productId") Long productId);


}
