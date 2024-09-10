package practice.newbalance.repository.item.query;

import practice.newbalance.dto.item.CouponDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomCouponRepository {

    List<CouponDto> findCouponAll(int offset, int limit);
    List<CouponDto> findCouponInUseList(Long memberId);

    List<CouponDto> findCouponsByCriteria(Long memberId, LocalDateTime startDate, LocalDateTime endDate, int offset, int limit);


}
