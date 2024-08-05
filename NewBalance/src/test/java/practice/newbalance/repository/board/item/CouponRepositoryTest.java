package practice.newbalance.repository.board.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import practice.newbalance.domain.item.Coupon;
import practice.newbalance.domain.member.Member;
import practice.newbalance.repository.MemberRepository;
import practice.newbalance.repository.item.CouponRepository;
import practice.newbalance.service.item.CouponServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static practice.newbalance.domain.item.CouponEnum.NEW;

@SpringBootTest
@ActiveProfiles("local")
class CouponRepositoryTest {

    @Autowired
    private  CouponServiceImpl couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @Transactional
    void test_lock2(){
        Member member = new Member();
        member.setName("test");
        em.persist(member);
        System.out.println("member.getId() = " + member.getId());
    }


    @Test
    @Transactional(readOnly = false)
    void test_lock1() throws InterruptedException{
    int executeNumber = 15;

    final ExecutorService executorService = Executors.newFixedThreadPool(10);
    final CountDownLatch countDownLatch = new CountDownLatch(executeNumber);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failCount = new AtomicInteger();
        Coupon coupon = new Coupon("benefit","title", LocalDateTime.of(2024, 12, 31, 0, 0), "code", NEW, 5);
        Member member = new Member();

        coupon.addMember(member);
        memberRepository.save(member);
        Coupon coupon2 = couponRepository.save(coupon);
        System.out.println("coupon2 = " + coupon2);

        List<Coupon> coupons = couponRepository.findAll();
        for(Coupon coupon1 : coupons) {
            System.out.println("쿠폰리스트"+coupon1);
        }
        em.flush();
        em.clear();

        for(int i = 0; i < executeNumber; i++ ) {
            executorService.execute(() -> {
                try {
                    couponService.issueCoupon(coupon.getId());
                    successCount.getAndIncrement();
                    System.out.println("성공");
                } catch (PessimisticLockingFailureException iae) {
                    System.out.println("iae.getMessage() = " + iae.getMessage());
                    failCount.getAndIncrement();
                }catch (Exception e) {
                    System.out.println("비관적 락 발생");
                    System.out.println("e.getCause() = " + e.getCause());
                    System.out.println("e = "+ e);
                    failCount.getAndIncrement();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("성공한 횟수  = " + successCount.get());
        System.out.println("실패한 횟수 = " + failCount.get());
        assertEquals(failCount.get() + successCount.get(), executeNumber);
    }

    @Test
    public void testIssueCoupon() {
        // Arrange
        Coupon coupon = new Coupon();
        coupon.setBenefit("Discount");
        coupon.setTitle("Test Coupon");
        coupon.setQuantity(10);

        // Save coupon
        coupon = couponRepository.save(coupon);

        // Extract coupon ID
        long couponId = coupon.getId();

        // Number of threads to run concurrently
        int numThreads = 5;

        // Create a latch to synchronize the threads
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Create and start threads
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    // Each thread issues a coupon
                    couponService.issueCoupon(couponId);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        // Wait for all threads to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("Main thread was interrupted while waiting: " + e.getMessage());
        }

        // Check the final quantity of the coupon
        Coupon finalCoupon = couponRepository.findByCouponId(couponId).orElse(null);
        assert finalCoupon != null;
        assert finalCoupon.getQuantity() == 5;
    }

    @Test
    public void 비관적락_쿠폰테스트() {
        // 쿠폰 객체 생성
        Coupon coupon = new Coupon();

        coupon.setBenefit("할인");
        coupon.setTitle("테스트 쿠폰");
        coupon.setQuantity(10);

        // 쿠폰 저장
        coupon = couponRepository.save(coupon);

        // 쿠폰 ID 추출
        long couponId = coupon.getId();

//        ExecutorService executor = Executors.newFixedThreadPool(15);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            futures.add(executor.submit(() -> {
                try {
                    couponService.issueCoupon(couponId);
                } catch (Exception e) {
                    System.out.println("Exception occureed = " + e.getMessage());
                }
            }));
        }

        // 모든 작업이 완료될 때까지 대기
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // 쿠폰의 수량이 여전히 10개인지 확인
//        Coupon savedCoupon = couponRepository.findByIdForUpdate(couponId).orElse(null);
//        assert savedCoupon != null;
//        assert savedCoupon.getQuantity() == 10; // 예상되는 수량은 변경되지 않음

        // 스레드 풀 종료
        executor.shutdown();
    }
}