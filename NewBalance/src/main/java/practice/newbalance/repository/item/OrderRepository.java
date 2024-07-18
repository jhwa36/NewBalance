package practice.newbalance.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.newbalance.domain.item.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
