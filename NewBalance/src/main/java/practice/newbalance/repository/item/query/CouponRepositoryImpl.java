package practice.newbalance.repository.item.query;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import practice.newbalance.dto.item.CouponDto;
import practice.newbalance.repository.item.query.CustomCouponRepository;

import java.util.List;

import static practice.newbalance.domain.item.QCoupon.coupon;

@Repository
public class CouponRepositoryImpl implements CustomCouponRepository {

    private final JPAQueryFactory queryFactory;

    @Autowired
    public CouponRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<CouponDto> findCouponAll(int offset, int limit) {
        return queryFactory.select(Projections.constructor(
                CouponDto.class,
                coupon.id,
                coupon.benefit,
                coupon.title,
                coupon.sDate,
                coupon.period,
                coupon.code,
                coupon.status,
                coupon.quantity
                    ))
                .from(coupon)
                .orderBy(coupon.id.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
