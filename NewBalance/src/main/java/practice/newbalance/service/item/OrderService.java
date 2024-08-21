package practice.newbalance.service.item;

import org.springframework.http.ResponseEntity;
import practice.newbalance.domain.item.Order;
import practice.newbalance.dto.item.CartInfoDto;

import java.util.List;

public interface OrderService {
    Long order(Long memberId, List<Long> carts);
    ResponseEntity<String> delOrder(Long orderId);
    Order findOrder(Long orderId);
    CartInfoDto getCartInfo(Long cartId);
}
