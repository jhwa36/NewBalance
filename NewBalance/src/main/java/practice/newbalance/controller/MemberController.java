package practice.newbalance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import practice.newbalance.config.security.CustomUserDetail;
import practice.newbalance.domain.member.DeliveryAddress;
import practice.newbalance.dto.member.DeliveryAddressDto;
import practice.newbalance.dto.member.MemberDto;
import practice.newbalance.service.MemberService;
import practice.newbalance.web.validator.CheckEmailValidator;
import practice.newbalance.web.validator.CheckUserIdValidator;


import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    //중복 체크 유효성 검사
    private final CheckUserIdValidator checkUserIdValidator;
    private final CheckEmailValidator checkEmailValidator;

    /**
     * 커스텀 유효성 검증
     */
    @InitBinder
    public void validatorBinder(WebDataBinder binder) {
        binder.addValidators(checkUserIdValidator);
        binder.addValidators(checkEmailValidator);
    }

    /**
     * 회원가입 폼 화면 이동
     */
    @GetMapping("/members/join")
    public String members(Model model) {
        model.addAttribute("memberDto", new MemberDto());
        return "/member/join";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/members/new")
    public String memberJoin(@Valid MemberDto memberDto, BindingResult bindingResult, Model model) {

        // 검증
        if(bindingResult.hasErrors()){
            //회원가입 실패 시 입력 데이터 값 유지
            model.addAttribute("memberDto", memberDto);

            //회원 가입 페이지로 리턴
            return "/member/join";
        }
        //회원 가입 성공
        memberService.join(memberDto);
        return "redirect:/members/login";
    }


    /**
     * 아이디 찾기 폼 화면 이동
     */
    @GetMapping("/members/inquiry")
    public String findIdForm(Model model){
        model.addAttribute("memberDto", new MemberDto());
        return "member/inquiryForm";
    }

    /**
     * 아이디 찾기
     * @param memberDto
     * @return
     */
    @ResponseBody
    @PostMapping("/members/inquiry")
    public Map<String, Object> inquiryFindId(@ModelAttribute MemberDto memberDto){
        log.info("start memberDto = {}", memberDto);
        return memberService.inquiryFindId(memberDto);
    }

    @ResponseBody
    @PostMapping("/members/inquiry/reset-pw")
    public Map<String, Object> inquiryResetPw(
            @RequestParam("userId") String userId,
            @RequestParam("name") String name,
            @RequestParam("phoneNumber") String phoneNumber
    ){
        return memberService.inquiryResetPw(userId, name, phoneNumber);
    }

    /**
     * 마이페이지 이동
     * @return
     */
    @GetMapping("/my")
    public String myPage(){
        return "member/user/myPage";
    }

    @GetMapping("/my/delivery-addr")
    public String deliveryAddressHome(
            Model model,
            @AuthenticationPrincipal CustomUserDetail customUserDetail){
        List<DeliveryAddress> addressList = memberService.getAddress(customUserDetail.getMember().getId());
        model.addAttribute("myContents", addressList);
        return "member/user/myPage-deliveryAddress";
    }

    /**
     * 배송지 추가 폼 이동
     * @return
     */
    @GetMapping("/my/delivery-addr-form")
    public String deliveryAddressAddForm(){
        return "member/user/myPage-deliveryAddForm";
    }

    /**
     * 배송지 수정 폼 이동
     * @param addrId
     * @param model
     * @return
     */
    @GetMapping("/my/delivery-addr/{addrId}")
    public String deliveryUpdateForm(
            @PathVariable("addrId") Long addrId,
            Model model
    ){
        DeliveryAddress detailAddress = memberService.getDetailAddress(addrId);
        model.addAttribute("deliveryAddr", detailAddress.toDTO());
        return "member/user/myPage-deliveryUpdateForm";
    }

    /**
     * 배송지 주소 추가
     * @param customUserDetail
     * @param dto
     * @return
     */
    @PostMapping("/my/delivery-addr")
    public ResponseEntity<String> addDeliveryAddress(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            DeliveryAddressDto dto
    ){
        return memberService.saveAddress(customUserDetail.getMember().getId(), dto);
    }

    @PutMapping("/my/delivery-addr/{addrId}")
    public ResponseEntity<String> updateDeliveryAddress(
            @PathVariable("addrId") long addrId,
            DeliveryAddressDto dto
    ){
        return memberService.updateAddress(addrId, dto);
    }

    /**
     * 배송지 주소 삭제
     * @param addrId
     * @return
     */
    @DeleteMapping("/my/delivery-addr/{addrId}")
    public ResponseEntity<String> DeleteDeliveryAddr(@PathVariable("addrId") long addrId){
        return memberService.deleteAddress(addrId);
    }

    /**
     * 마이페이지 쿠폰페이지 이동
     * @return
     */
    @GetMapping("/my/couponList")
    public String couponListHome(Model model,
                                 @AuthenticationPrincipal CustomUserDetail customUserDetail){
        return "member/user/myPage-couponList";
    }

    @PostMapping("/my/couponAdd")
    public ResponseEntity<String> couponAdd(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @RequestParam("code") String code) {

        try {
            String result = memberService.registorCoupon(customUserDetail.getMember().getId(), code);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("쿠폰 등록 중 오류가 발생했습니다.");
        }
    }
}
