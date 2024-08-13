package practice.newbalance.domain.item;


import jakarta.persistence.*;
import lombok.*;
import practice.newbalance.domain.ModifierEntity;
import practice.newbalance.domain.member.Member;
import practice.newbalance.dto.item.CouponDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends ModifierEntity {

    @Id
    @GeneratedValue
    @Column(name = "coupon_id")
    private Long id;

    @Column(name = "benefit")
    private String benefit;

    @Column(name = "title")
    private String title;

    @Column(name = "sDate")
    private LocalDateTime sDate;

    @Column(name = "peroid")
    private LocalDateTime period;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CouponEnum status;

    @Column(name = "quantity")
    private int quantity; // 쿠폰 수량

    //    @JsonIgnore // 양방향 걸린 곳은 꼭 한곳을 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void addMember(Member member){
        this.member = member;
        member.getCoupons().add(this);
    }

    public void isCoupon() {
        if (quantity <= 0) {
            throw new IllegalStateException("수량이 이미 부족합니다");
        }
        quantity -= 1;
    }

    public CouponDto toDto(){
        CouponDto couponDto = CouponDto.builder()
                .id(id)
                .benefit(benefit)
                .title(title)
                .sDate(sDate)
                .period(period)
                .code(code)
                .status(status)
                .quantity(quantity)
                .build();
        return couponDto;
    }

    public Coupon(String benefit, String title, LocalDateTime sDate, LocalDateTime period,  String code, CouponEnum status, int quantity) {
        this.benefit = benefit;
        this.title = title;
        this.sDate = sDate;
        this.period = period;
        this.code = code;
        this.status = status;
        this.quantity = quantity;
    }
}
