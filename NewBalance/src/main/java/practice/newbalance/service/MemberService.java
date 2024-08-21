package practice.newbalance.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.item.Coupon;
import practice.newbalance.domain.item.CouponEnum;
import practice.newbalance.domain.member.DeliveryAddress;
import practice.newbalance.domain.member.Member;
import practice.newbalance.dto.item.CouponDto;
import practice.newbalance.dto.member.DeliveryAddressDto;
import practice.newbalance.dto.member.MemberDto;
import practice.newbalance.repository.MemberRepository;
import practice.newbalance.repository.item.CouponRepository;
import practice.newbalance.repository.item.query.CouponRepositoryImpl;
import practice.newbalance.repository.user.DeliveryAddressRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CouponRepository couponRepository;
    private final CouponRepositoryImpl couponRepositoryImpl;

    public Long join(MemberDto memberDto) {

        if(memberDto.getUserId().equals("admin")){
            return memberRepository.save(memberDto.toEntity(bCryptPasswordEncoder, "ROLE_ADMIN")).getId();
        }
        return memberRepository.save(memberDto.toEntity(bCryptPasswordEncoder, "ROLE_USER")).getId();
    }

    public Map<String, Object> inquiryFindId(MemberDto dto){
        Map<String, Object> result = new HashMap<>();
        String findUserId = memberRepository.findInquiryIdByNameAndPhoneNumber(
                dto.getName(),
                dto.getPhoneNumber()
        );
        if(findUserId == null){
            result.put("userId", null);
            return result;
        }

        result.put("userId", findUserId);
        result.put("name", dto.getName());
        result.put("phoneNumber", dto.getPhoneNumber());

        return result;
    }

    @Transactional
    public Map<String, Object> inquiryResetPw(String userId, String name, String phoneNumber){
        Map<String, Object> result = new HashMap<>();
        Optional<Member> findMember = memberRepository.findByUserId(userId, name, phoneNumber);

        if(findMember.isPresent()){
            String tempPw = UUID.randomUUID().toString().substring(0, 8);

            //todo:임시 비밀번호 이메일 전송 로직 필요
            findMember.get().setPassword(bCryptPasswordEncoder.encode(tempPw));
            result.put("result", true);
        }else{
            result.put("result", false);
        }

        return result;
    }

    public List<MemberDto> findMemberAll(int offset, int limit){
        return memberRepository.findMemberAll(offset, limit);
    }

    public long getMemberCount(){
        return memberRepository.count();
    }


    /**
     * 회원의 배송지 전체 조회
     * @param id
     * @return List<DeliveryAddress>
     */
    public List<DeliveryAddress> getAddress(Long id){
        return deliveryAddressRepository.findByMemberId(id);
    }

    /**
     * 배송지 상세 조회
     * @param id
     * @return DeliveryAddressDto
     */
    public DeliveryAddress getDetailAddress(Long id){
        return deliveryAddressRepository.findById(id)
                .orElseThrow(
                        () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
                );
    }

    /**
     * 배송지 추가
     * @param id
     * @param dto
     * @return ResponseEntity<String>
     */
    @Transactional
    public ResponseEntity<String> saveAddress(Long id, DeliveryAddressDto dto){
        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setRecipient(dto.getRecipient());
        deliveryAddress.setDestination(dto.getDestination());
        deliveryAddress.setRecipientNumber(dto.getRecipientNumber());
        deliveryAddress.setZipCode(dto.getZipCode());
        deliveryAddress.setAddress(dto.getAddress());
        deliveryAddress.setDetailAddress(dto.getDetailAddress());
        deliveryAddress.setMemberId(id);

        // 기본 배송지 설정 변경
        changeDefaultAddress(dto, deliveryAddress);

        deliveryAddressRepository.save(deliveryAddress);

        return ResponseEntity.ok("success");
    }

    /**
     * 기본 배송지 변경
     * @param dto
     * @param deliveryAddress
     */
    private void changeDefaultAddress(DeliveryAddressDto dto, DeliveryAddress deliveryAddress) {
        if(dto.getDefaultYN()){
            Optional<DeliveryAddress> findAddr = deliveryAddressRepository.findByDefaultYN(true);
            findAddr.ifPresent(address -> address.setDefaultYN(!dto.getDefaultYN()));
            deliveryAddress.setDefaultYN(true);
        }else{
            deliveryAddress.setDefaultYN(false);
        }
    }

    /**
     * 배송지 수정
     * @param addrId
     * @param dto
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> updateAddress(long addrId, DeliveryAddressDto dto){
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(addrId).
                orElseThrow(
                        () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
                );

        deliveryAddress.setRecipient(dto.getRecipient());
        deliveryAddress.setRecipientNumber(dto.getRecipientNumber());
        deliveryAddress.setDestination(dto.getDestination());
        deliveryAddress.setZipCode(dto.getZipCode());
        deliveryAddress.setAddress(dto.getAddress());
        deliveryAddress.setDetailAddress(dto.getDetailAddress());

        changeDefaultAddress(dto, deliveryAddress);

        deliveryAddressRepository.save(deliveryAddress);
        return ResponseEntity.ok("success");
    }

    /**
     * 배송지 삭제
     * @param addrId
     * @return ResponseEntity<String>
     */
    @Transactional
    public ResponseEntity<String> deleteAddress(Long addrId){
        deliveryAddressRepository.deleteById(addrId);
        return ResponseEntity.ok("success");
    }

    @Transactional
    public String registorCoupon(Long memberId, String code) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND));

        // 유효성 검사 및 쿠폰 등록
        if(couponRepository.existsByMemberAndCode(member, code)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.COUPON_ALREADY_REGISTERED);
        }

        // 유효한 쿠폰인지 확인(DB에 존재하는지 확인)
        Coupon existingCoupon = couponRepository.findByCodeForUpdate(code);
        if(existingCoupon == null){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_COUPON);
        }

        // 쿠폰 수량이 남아있는지 확인
        if(existingCoupon.getQuantity() <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.COUPON_OUT_OF_STOCK);
        } else if(existingCoupon.getStatus() == CouponEnum.EXPIRED) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.COUPON_EXPIRED);
        } else if(existingCoupon.getStatus() == CouponEnum.USED) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.COUPON_USED);
        }

        Coupon newCoupon = new Coupon();
        newCoupon.setCode(existingCoupon.getCode());
        newCoupon.setBenefit(existingCoupon.getBenefit());
        newCoupon.setTitle(existingCoupon.getTitle());
        newCoupon.setSDate(existingCoupon.getSDate());
        newCoupon.setPeriod(existingCoupon.getPeriod());
        newCoupon.setQuantity(1); // 회원에게 발급된 개별 쿠폰의 수량은 1로 설정
        newCoupon.setStatus(CouponEnum.NOT_USED);
        newCoupon.setMember(member);

        // 기존 쿠폰의 수량 감소
        existingCoupon.setQuantity(existingCoupon.getQuantity() -1);
        couponRepository.save(existingCoupon);

        // 새로운 쿠폰 저장
        couponRepository.save(newCoupon);

        member.addCoupon(newCoupon);
        memberRepository.save(member);

        return "쿠폰이 성공적으로 등록되었습니다.";
    }

    /**
     * 사용자 쿠폰 목록 조회 및 날짜 검색
     * @param memberId
     * @param startDate
     * @param endDate
     * @param offset
     * @param limit
     * @return
     */
    public List<CouponDto> memberCouponse(Long memberId, LocalDateTime startDate, LocalDateTime endDate, int offset, int limit){
        List<CouponDto> result = couponRepositoryImpl.findCouponsByCriteria(memberId, startDate, endDate, offset, limit);
        return result;
    }

    public long getCouponCount(Long memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return couponRepositoryImpl.getCouponCount(memberId, startDate, endDate);
    }

}
