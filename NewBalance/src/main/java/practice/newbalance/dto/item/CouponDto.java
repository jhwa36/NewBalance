package practice.newbalance.dto.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import practice.newbalance.domain.item.Coupon;
import practice.newbalance.domain.item.CouponEnum;
import practice.newbalance.domain.member.Member;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {
    private Long id;
    private String benefit;
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime period;
    private String code;
    private CouponEnum status;
    private int quantity;

    private Member member;

    private String formattedSDate;
    private String formattedPeriod;

    @QueryProjection
    public CouponDto(Long id, String benefit, String title, LocalDateTime sDate, LocalDateTime period, String code, CouponEnum status, int quantity) {
        this.id = id;
        this.benefit = benefit;
        this.title = title;
        this.sDate = sDate;
        this.period = period;
        this.code = code;
        this.status = status;
        this.quantity = quantity;
    }

    public Coupon toEntity(){
        Coupon coupon = Coupon.builder()
                .id(id)
                .benefit(benefit)
                .title(title)
//                .period(LocalDateTime.parse(period.toString(), DateTimeFormatter.ISO_DATE_TIME))
                .sDate(sDate)
                .period(period)
                .code(code)
                .status(status)
                .quantity(quantity)
                .build();
        return coupon;
    }
}
