package practice.newbalance.service.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.item.Coupon;
import practice.newbalance.dto.item.CouponDto;
import practice.newbalance.repository.item.CouponRepository;
import practice.newbalance.repository.item.query.CustomCouponRepository;

import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CustomCouponRepository customCouponRepository;

    @Autowired
    public CouponServiceImpl(CouponRepository couponRepository, CustomCouponRepository customCouponRepository) {
        this.couponRepository = couponRepository;
        this.customCouponRepository = customCouponRepository;
    }

    @Override
    public List<CouponDto> findCouponAll(int offset, int limit) {
        return customCouponRepository.findCouponAll(offset, limit);
    }

    @Override
    public long getCouponCount() {
        return couponRepository.count();
    }

    @Override
    public CouponDto addCoupon(CouponDto couponDto) {
        Coupon coupon = couponRepository.save(couponDto.toEntity());
        return coupon.toDto();
    }

    @Override
    public CouponDto updateCoupon(Long couponId, CouponDto couponDto) {
        Coupon coupon = couponRepository.findByCouponId(couponId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA));
        coupon.setBenefit(couponDto.getBenefit());
        coupon.setCode(couponDto.getCode());
        coupon.setPeriod(couponDto.getPeriod());
        coupon.setQuantity(couponDto.getQuantity());
        coupon.setTitle(couponDto.getTitle());
        coupon.setStatus(couponDto.getStatus());
        return couponRepository.save(coupon).toDto();
    }


    @Override
    public void deleteCoupon(Long couponId) {
        couponRepository.deleteById(couponId);
    }

    // pessimistic lock
    @Transactional
    public void issueCoupon(long couponId){
        Coupon coupon = couponRepository.findByCouponId(couponId).orElseThrow(() -> new IllegalArgumentException("등록되지 않은 쿠폰입니다."));

        if (coupon.getQuantity() <= 0) {
            throw new IllegalArgumentException("수량이 부족합니다");
        }

        coupon.isCoupon();
        couponRepository.save(coupon);
    }
}