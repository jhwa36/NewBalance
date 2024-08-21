package practice.newbalance.domain.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import practice.newbalance.domain.member.Member;

@Entity
@Setter @Getter
@Table(name = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {
    @Id @GeneratedValue
    @Column(name = "cart_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_opt_id")
    private ProductOption productOption;

    @Column(name = "price")
    private int price;

    @Column(name = "count")
    private int count;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public static Cart createCart(Member member, Product product, ProductOption option, int price, int count){
        Cart cart = new Cart();
        cart.setMember(member);
        cart.saveItem(cart, product, option, price, count);
        option.removeStock(count);
        return cart;
    }

    public void saveItem(Cart cart, Product product, ProductOption option, int price, int count){
        cart.setProduct(product);
        cart.setProductOption(option);
        cart.setCount(count);
        cart.setPrice(price);
    }

    public void updateOption(Cart cart, ProductOption option){
        //이전 옵션의 수량 증가
        getProductOption().addStock(getCount());
        //옵션 변경
        cart.setProductOption(option);
        //변경된 옵션의 수량 감소
        option.removeStock(getCount());
    }

    public void updateCount(Cart cart, int count){
        ProductOption productOption = cart.getProductOption();
        productOption.addStock(cart.getCount());
        productOption.removeStock(count);
        cart.setCount(count);
        cart.setPrice(cart.getProduct().getPrice() * count);
    }

    public void cancel(){
        getProductOption().addStock(getCount());
    }
}
