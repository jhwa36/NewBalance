package practice.newbalance.service.item;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import practice.newbalance.domain.item.Order;
import practice.newbalance.domain.member.Member;
import practice.newbalance.repository.MemberRepository;
import practice.newbalance.repository.item.OrderRepository;
import practice.newbalance.repository.item.ProductOptionRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductOptionRepository optionRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void order(){
        Long memberId = 1L;
        Member member = memberRepository.findById(memberId).get();

        List<Long> cartIdList = new ArrayList<>();
        cartIdList.add(252L);
        cartIdList.add(253L);

        Long orderId = orderService.order(memberId, cartIdList);
        Order order = orderRepository.findById(orderId).get();

        Assertions.assertThat(order).isNotNull();
//        Assertions.assertThat(order.getPrice()).isEqualTo(657000);
    }

    @Test
    public void order_cancel(){
        Long orderId = 1L;
        Long optionId = 53L;

        Order order = orderRepository.findById(orderId).orElseThrow(
                RuntimeException::new
        );

        orderService.delOrder(orderId);
        //주문 유무 확인
        Assertions.assertThat(orderRepository.findById(orderId)).isNotEmpty();
        //선택한 옵션 수량 원상복구 확인
        Assertions.assertThat(optionRepository.findById(optionId).get().getQuantity()).isEqualTo(23);
    }
}