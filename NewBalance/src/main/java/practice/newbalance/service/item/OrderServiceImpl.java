package practice.newbalance.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.domain.item.Order;
import practice.newbalance.domain.member.DeliveryAddress;
import practice.newbalance.domain.member.Member;
import practice.newbalance.repository.MemberRepository;
import practice.newbalance.repository.item.CartRepository;
import practice.newbalance.repository.item.OrderRepository;
import practice.newbalance.repository.user.DeliveryAddressRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final DeliveryAddressRepository addressRepository;

    @Transactional
    @Override
    public Long order(Long memberId, List<Long> carts) {
        List<Cart> cartList = new ArrayList<>();
        Long orderId = null;

        //엔티티 조회
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );

        for (Long cartId : carts) {
            Cart cart = cartRepository.findById(cartId).orElseThrow(
                    () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
            );
            cartList.add(cart);
        }

        List<DeliveryAddress> addressList = member.getAddress();

        for(DeliveryAddress address : addressList){
            if(address.getDefaultYN()){
                Order order = Order.createOrder(member, address, cartList);
                orderRepository.save(order);
                orderId = order.getId();
            }
        }
        if(orderId == null){
            log.info("orderId = {}", orderId);
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.REQUIRED_SETTING_ADDRESS);
        }

        return orderId;


//        member.getAddress().stream()
//                .filter(DeliveryAddress::getDefaultYN)
//                .forEach(address -> {
//                    //주문 생성
//                    Order order = Order.createOrder(member, address, cartList);
//                    orderRepository.save(order);
//                });
//        return ResponseEntity.ok("success");
    }

    @Transactional
    @Override
    public ResponseEntity<String> delOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );
        order.cancel();
        return ResponseEntity.ok("success");
    }
}
