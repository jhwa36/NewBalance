package practice.newbalance.repository.item.query;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import practice.newbalance.domain.item.CouponEnum;
import practice.newbalance.dto.item.CouponDto;
import practice.newbalance.repository.item.query.CustomCouponRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Override
    public List<CouponDto> findCouponInUseList(Long memberId) {
        return queryFactory
                .select(
                        Projections.constructor(
                            CouponDto.class,
                            coupon.id,
                            coupon.benefit,
                            coupon.title,
                            coupon.sDate,
                            coupon.period,
                            coupon.code,
                            coupon.status
                        )
                )
                .from(coupon)
                .where(coupon.member.id.eq(memberId)
                        .and(coupon.status.eq(CouponEnum.USED)))
                .fetch();
    }

    @Override
    public List<CouponDto> findCouponsByCriteria(Long memberId, LocalDateTime startDate, LocalDateTime endDate, int offset, int limit) {
        JPAQuery<?> query = queryFactory.select(Projections.fields(CouponDto.class,
                coupon.id.as("id"),
                coupon.benefit.as("benefit"),
                coupon.title.as("title"),
                coupon.sDate.as("sDate"),
                coupon.period.as("period"),
                coupon.code.as("code"),
                coupon.status.as("status"),
                coupon.quantity.as("quantity"))
        )
                .from(coupon)
                .where(coupon.member.id.eq(memberId)
                        .and(coupon.status.eq(CouponEnum.NOT_USED)));
        // 날짜 필터링 조건 추가 (null 체크 포함)
        if(startDate != null && endDate != null) {
            query = query.where(
                    coupon.sDate.loe(endDate) // 쿠폰의 시작일이 범위의 종료일 이전이거나 같음
                            .and(coupon.period.goe(startDate)) // 쿠폰의 종료일이 범위의 시작일 이후이거나 같음
            );
        }

        return (List<CouponDto>) query.offset(offset).limit(limit).fetch();
    }

    public long getCouponCount(Long memberId, LocalDateTime startDate, LocalDateTime endDate) {
        JPAQuery<?> query = queryFactory.select(coupon.count())
                .from(coupon)
                .where(coupon.member.id.eq(memberId)
                        .and(coupon.status.eq(CouponEnum.NOT_USED)));
        if(startDate != null && endDate != null) {
            query = query.where(
                    coupon.sDate.loe(endDate)
                            .and(coupon.period.goe(startDate))
            );
        }
        return (long) query.fetchOne();
    }
}
