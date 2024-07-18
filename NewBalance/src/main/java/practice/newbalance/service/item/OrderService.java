package practice.newbalance.service.item;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    Long order(Long memberId, List<Long> carts);
    ResponseEntity<String> delOrder(Long orderId);
}
