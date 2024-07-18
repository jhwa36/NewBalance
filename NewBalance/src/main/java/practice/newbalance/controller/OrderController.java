package practice.newbalance.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import practice.newbalance.config.security.CustomUserDetail;
import practice.newbalance.service.item.OrderService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/")
    public String orderHome(){
        return "item/orders";
    }

    @PostMapping("/")
    public ResponseEntity<String> order(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @RequestParam("carts") String carts) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        List<Long> cartList = om.readValue(carts, new TypeReference<List<Long>>() {
        });

        orderService.order(customUserDetail.getMember().getId(), cartList);
        return ResponseEntity.ok("success");
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> delOrder(@PathVariable("orderId") Long orderId){
        return orderService.delOrder(orderId);
    }
}
