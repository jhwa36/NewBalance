package practice.newbalance.repository.item;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import practice.newbalance.domain.item.Coupon;
import practice.newbalance.domain.member.Member;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("SELECT c FROM Coupon c WHERE c.id = :couponId")
    Optional<Coupon> findByCouponId(@Param("couponId") Long couponId);

    // 비관적 락 사용 entity 조회 시 select for update 쿼리 사용하여 특정 레코드 조회하고 락을 설정
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.code = :code")
    Coupon findByCodeForUpdate(@Param("code") String code);

    boolean existsByMemberAndCode(Member member, String code);

    boolean existsByCode(String Code);
}
