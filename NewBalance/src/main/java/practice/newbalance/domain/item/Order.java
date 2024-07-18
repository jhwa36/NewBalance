package practice.newbalance.domain.item;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.member.DeliveryAddress;
import practice.newbalance.domain.member.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Getter @Setter
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "price")
    private int price;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_addr_id")
    private DeliveryAddress deliveryAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Cart> cartList = new ArrayList<>();

    public static Order createOrder(Member member, DeliveryAddress address, List<Cart> carts){
        int totalPrice = 0;
        Order order = new Order();
        order.setMember(member);
        order.setDeliveryAddress(address);
        for(Cart cart : carts){
            totalPrice += cart.getPrice();
            order.addCart(cart);
        }
        order.setOrderDate(LocalDateTime.now());
        order.setCode(UUID.randomUUID().toString().substring(0, 8));
        order.setPrice(totalPrice);
        order.setStatus(OrderStatus.PAYMENT);

        return order;
    }

    //편의 메소드
    public void addCart(Cart cart){
        this.cartList.add(cart);
        cart.setOrder(this);
    }

    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
    }

    public void setDeliveryAddress(DeliveryAddress deliveryAddress){
        this.deliveryAddress = deliveryAddress;
        deliveryAddress.setOrder(this);
    }

    //비즈니스 로직
    public void cancel(){
        if(getStatus() == OrderStatus.COMPLETE || getStatus() == OrderStatus.CONFIRMATION){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DELIVERED_PRODUCT);
        }
        this.setStatus(OrderStatus.CANCEL);
        for (Cart cart : cartList) {
            cart.cancel();
        }
    }
}
