package practice.newbalance.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.domain.item.Order;
import practice.newbalance.domain.item.OrderStatus;
import practice.newbalance.domain.member.DeliveryAddress;
import practice.newbalance.domain.member.Member;
import practice.newbalance.dto.item.CartInfoDto;
import practice.newbalance.repository.MemberRepository;
import practice.newbalance.repository.item.CartRepository;
import practice.newbalance.repository.item.OrderRepository;
import practice.newbalance.repository.item.query.CustomCartRepository;
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
    private final CustomCartRepository customCartRepository;
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

        //장바구니 정보 조회
        for (Long cartId : carts) {
            Cart cart = cartRepository.findById(cartId).orElseThrow(
                    () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
            );
            cartList.add(cart);
        }

        List<DeliveryAddress> addressList = member.getAddress();

        //배송지 설정이 안되어 있는 경우
        if(addressList.isEmpty()){
            Order order = Order.createOrder(member, cartList);
            orderRepository.save(order);
            orderId = order.getId();
        }else{
            for(DeliveryAddress address : addressList){
                if(address.getDefaultYN()){
                    Order order = Order.createOrder(member, address, cartList);
                    orderRepository.save(order);
                    orderId = order.getId();
                }
            }
        }

        if(orderId == null){
            log.info("orderId = {}", orderId);
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.REQUIRED_SETTING_ADDRESS);
        }

        return orderId;

    }

    public Order findOrder(Long orderId){
        return orderRepository.findOrderById(orderId)
                .orElseThrow(
                        () -> new CustomException(
                                HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA
                        )
                );
    }

    @Transactional
    @Override
    public ResponseEntity<String> delOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );
        //cart 테이블 데이터 최신화
        List<Cart> cartByOrderId = cartRepository.findCartByOrderId(orderId);
        for(Cart cart : cartByOrderId){
            cart.setOrder(null);
        }
        //order 테이블 데이터 삭제
        orderRepository.delete(order);
        return ResponseEntity.ok("success");
    }

    public CartInfoDto getCartInfo(Long cartId){
        return customCartRepository.getProductInCart(cartId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );
    }

    //주문 상태 변경
    @Transactional
    public ResponseEntity<String> updateStatusOrder(Long orderId, String status){
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );
        switch (status){
            case "PAYMENT" : {
                order.setStatus(OrderStatus.PAYMENT);
                break;
            }
            case "WAITING" : {
                order.setStatus(OrderStatus.WAITING);
            }
            case "SHIPPING" : {
                order.setStatus(OrderStatus.SHIPPING);
            }
            case "COMPLETE" : {
                order.setStatus(OrderStatus.COMPLETE);
            }
            case "CONFIRMATION" : {
                order.setStatus(OrderStatus.CONFIRMATION);
            }
            default : {
                throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.ERROR_CHANGE_ORDER_STATUS);
            }
        }
        return ResponseEntity.ok("success");
    }

    //주문 시 배송지 설정 변경
    @Transactional
    public ResponseEntity<String> updateDeliveryAddress(
            Long orderId,
            DeliveryAddress entity
    ){
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );
        order.setDeliveryAddress(entity);
        return ResponseEntity.ok("success");
    }

    //주문 시 결제 금액 저장
    @Transactional
    public ResponseEntity<String> updatePaymentAmount(Long orderId, String benefit, Model model){
        //데이터 조회
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );

        double discountRate = Double.parseDouble(benefit) / 100;
        int discountAmount = (int) (order.getTotalPrice() * discountRate);
        int charge = order.getTotalPrice() < 5000 ? 3000 : 0;

        //할인가격, 결제 금액 최신화
        order.setDiscountAmount(discountAmount);
        order.setPaymentAmount(order.getTotalPrice() - discountAmount + charge);

        //model에 데이터 저장
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("paymentAmount", order.getPaymentAmount());
        model.addAttribute("charge", charge);

        return ResponseEntity.ok("success");
    }



}
