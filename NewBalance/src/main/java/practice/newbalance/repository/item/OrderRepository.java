package practice.newbalance.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import practice.newbalance.domain.item.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(
            "select o from Order o " +
            "join fetch o.deliveryAddress " +
            "join fetch o.member " +
            "join fetch o.cartList " +
            "where o.id = :orderId"
    )
    Optional<Order> findOrderById(@Param("orderId") Long orderId);

}
