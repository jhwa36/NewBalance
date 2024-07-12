package practice.newbalance.service.item;

import org.springframework.stereotype.Service;
import practice.newbalance.domain.item.Coupon;
import practice.newbalance.dto.item.CouponDto;

import java.util.List;

public interface CouponService {

    // 쿠폰 목록
    List<CouponDto> findCouponAll(int offset, int limit);

    // 등록된 쿠폰의 수
    long getCouponCount();

    // 쿠폰 등록
    CouponDto addCoupon(CouponDto couponDto);
    CouponDto updateCoupon(Long couponId, CouponDto couponDto);

    void deleteCoupon(Long couponId);


}
