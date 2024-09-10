package practice.newbalance.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import practice.newbalance.domain.item.Cart;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("select c from Cart c join fetch c.product where c.id = :cartId")
    Optional<Cart> findById(@Param("cartId") Long cartId);

    @Query("select c from Cart c join fetch c.product where c.order.id is null")
    List<Cart> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);

    @Query("select c from Cart c where c.order.id = :orderId")
    List<Cart> findCartByOrderId(@Param("orderId") Long orderId);
}
