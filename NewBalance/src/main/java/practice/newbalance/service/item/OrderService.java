package practice.newbalance.service.item;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import practice.newbalance.domain.item.Order;
import practice.newbalance.domain.member.DeliveryAddress;
import practice.newbalance.dto.item.CartInfoDto;

import java.util.List;

public interface OrderService {
    Long order(Long memberId, List<Long> carts);
    ResponseEntity<String> delOrder(Long orderId);
    Order findOrder(Long orderId);
    CartInfoDto getCartInfo(Long cartId);
    ResponseEntity<String> updateStatusOrder(Long orderId, String status);
    ResponseEntity<String> updatePaymentAmount(Long orderId, String benefit, Model model);
    ResponseEntity<String> updateDeliveryAddress(Long orderId, DeliveryAddress entity);
}
