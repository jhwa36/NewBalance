package practice.newbalance.service.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import practice.newbalance.domain.item.Coupon;
import practice.newbalance.repository.item.CouponRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("local")
public class CouponTest {

    @Autowired
    private CouponServiceImpl couponServiceImpl;

    @Autowired
    private CouponRepository couponRepository;

    private Long couponId;

    @BeforeEach
    void setUp(){
        Coupon coupon = new Coupon();
//        coupon.setId(1L);
        coupon.setQuantity(5);
        Coupon saveCoupon = couponRepository.save(coupon);
        couponId = saveCoupon.getId();
    }

    @Test
    void 동시에_10명이_쿠폰을요청하면_정확히5명만_성공해야함() throws InterruptedException {
        int thredCount  = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(thredCount);
        CountDownLatch latch = new CountDownLatch(thredCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for(int i = 0; i < thredCount; i++){
            executorService.execute(() -> {
                try{
                    couponServiceImpl.issueCoupon(couponId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println("성공한 사용자 수 successCount = " + successCount.get());
        System.out.println("실패한 사용자 수 failCount = " + failCount.get());

        assertEquals(5, successCount.get());
        assertEquals(5, failCount.get());

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        assertEquals(0, coupon.getQuantity());

        executorService.shutdown();
    }


}
