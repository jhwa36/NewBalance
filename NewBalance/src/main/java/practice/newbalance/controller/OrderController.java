package practice.newbalance.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.config.security.CustomUserDetail;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.domain.item.Order;
import practice.newbalance.dto.item.CartInfoDto;
import practice.newbalance.service.item.OrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public String orderHome(
            @PathVariable("orderId") Long orderId,
            Model model
    ){
        System.out.println("orderId = " + orderId);
        //주문조회
        Order order = orderService.findOrder(orderId);

        //쿠폰조회
        //todo: 쿠폰 조회기능 완료 시 추가예정

        //주문내역 정보 조회
        List<CartInfoDto> cartList = new ArrayList<>();

        //총 합계 금액 및 주문내역 정보 조회
        order.getCartList()
                .forEach(cart -> {
                    CartInfoDto cartInfo = orderService.getCartInfo(cart.getId());
                    cartList.add(cartInfo);
                });

        int sum = order.getCartList().stream().mapToInt(Cart::getPrice).sum();

        model.addAttribute("cartList", cartList);
        model.addAttribute("productInfo", order.getCartList());
        model.addAttribute("deliveryAddr", order.getDeliveryAddress());
        model.addAttribute("totalPrice", sum);
//        model.addAttribute("couponList", couponList);

        return "item/orders";
    }

    @PostMapping("/")
    public ResponseEntity order(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @RequestParam("carts") String carts,
            Model model
    ) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        List<Long> cartList = om.readValue(carts, new TypeReference<List<Long>>() {
        });
        if(cartList.isEmpty()){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DONT_CREATE_ORDER);
        }

        //주문 생성
        Long order = orderService.order(customUserDetail.getMember().getId(), cartList);
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("id", order);

        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> delOrder(@PathVariable("orderId") Long orderId){
        return orderService.delOrder(orderId);
    }
}
