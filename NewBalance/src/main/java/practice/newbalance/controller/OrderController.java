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
import practice.newbalance.domain.item.Order;
import practice.newbalance.domain.member.DeliveryAddress;
import practice.newbalance.dto.item.CartInfoDto;
import practice.newbalance.dto.item.CouponDto;
import practice.newbalance.dto.member.DeliveryAddressDto;
import practice.newbalance.service.MemberService;
import practice.newbalance.service.item.CouponService;
import practice.newbalance.service.item.OrderService;
import practice.newbalance.utils.InfoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final MemberService memberService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final InfoUtils infoUtils;


    @GetMapping("/{orderId}")
    public String orderHome(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @PathVariable("orderId") Long orderId,
            Model model
    ){
        //주문조회
        Order order = orderService.findOrder(orderId);
        //쿠폰조회
        List<CouponDto> useCouponList = couponService.findCouponInUseList(customUserDetail.getMember().getId());

        //총 합계 금액 및 주문내역 정보 조회
        List<CartInfoDto> cartList = new ArrayList<>();

        order.getCartList()
                .forEach(cart -> {
                    CartInfoDto cartInfo = orderService.getCartInfo(cart.getId());
                    cartList.add(cartInfo);
                });


        model.addAttribute("orderId", order.getId());
        model.addAttribute("cartList", cartList);
        model.addAttribute("deliveryAddr", order.getDeliveryAddress());
        model.addAttribute("totalPrice", order.getTotalPrice());
        model.addAttribute("discountAmount", order.getDiscountAmount());
        model.addAttribute("paymentAmount", order.getPaymentAmount());
        model.addAttribute("member", order.getMember());
        model.addAttribute("emailList", infoUtils.getEmail());
        model.addAttribute("couponList", useCouponList);

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

    //주문 상태 변경
    @PutMapping("/success")
    public ResponseEntity<String> changeStatusOrder(@RequestBody Map<String, Object> json){

        return orderService.updateStatusOrder(
                Long.parseLong(json.get("orderId").toString()),
                json.get("status").toString());
    }

    //결제 예상 금액 최신화
    @PatchMapping("/{orderId}")
    public ResponseEntity<String> changePaymentAmount(
            @PathVariable("orderId") Long orderId, @RequestParam("benefit") String benefit, Model model){

        return orderService.updatePaymentAmount(orderId, benefit, model);
    }

    //배송지 주소 최신화
    @PatchMapping("/{orderId}/address")
    public ResponseEntity<String> updateDeliveryAddress(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @PathVariable("orderId") Long orderId,
            DeliveryAddressDto dto
    ){
        Long memberId = customUserDetail.getMember().getId();
        Long addressId = memberService.saveAddress(memberId, dto);

        DeliveryAddress addressEntity = memberService.getDetailAddress(addressId);

        return orderService.updateDeliveryAddress(orderId, addressEntity);
    }

    //주문 삭제
    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> delOrder(@PathVariable("orderId") Long orderId){
        return orderService.delOrder(orderId);
    }

    //주문 완료 페이지 이동
    @GetMapping("/{orderId}/success")
    public String successHome(@PathVariable("orderId") Long orderId, Model model){

        Order order = orderService.findOrder(orderId);

        List<CartInfoDto> cartList = new ArrayList<>();

        order.getCartList()
                .forEach(cart -> {
                    CartInfoDto cartInfo = orderService.getCartInfo(cart.getId());
                    cartList.add(cartInfo);
                });

        model.addAttribute("address", order.getDeliveryAddress().toDTO());
        model.addAttribute("cartList", cartList);
        return "item/success";
    }
}
