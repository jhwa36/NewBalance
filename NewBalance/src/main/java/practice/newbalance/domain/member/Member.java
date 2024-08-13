package practice.newbalance.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import practice.newbalance.domain.board.Notice;
import practice.newbalance.domain.item.Coupon;
import practice.newbalance.domain.item.Order;
import practice.newbalance.domain.item.ProductOption;
import practice.newbalance.dto.member.MemberDto;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(name = "id")
    private String userId;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "sex")
    private String sex;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "role")
    private String role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coupon> coupons = new ArrayList<>();

    @OneToMany(mappedBy = "memberId")
    private List<DeliveryAddress> address = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    public MemberDto toDTO() {
        MemberDto memberDto = MemberDto.builder()
                .userId(userId)
                .password(password)
                .name(name)
                .sex(sex)
                .email(email)
                .phoneNumber(getPhoneNumber())
                .role(role)
                .build();
        return memberDto;
    }

    public void addCoupon(Coupon coupon){
        this.coupons.add(coupon);
        coupon.setMember(this);
    }
}
