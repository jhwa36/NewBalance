package practice.newbalance.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //404
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "등록되지 않은 회원입니다."),

    //400
    UNKNOWN("UNKNOWN", "알 수 없는 에러가 발생하였습니다"),
    NOT_EXISTED_EMAIL("NOT_EXISTED_EMAIL", "존재하지 않는 회원입니다."),
    WRONG_PASSWORD("WRONG_PASSWORD", "틀린 비밀번호 입니다."),
    NOT_EXISTED_DATA("NOT_EXISTED_DATA", "데이터 조회에 실패했습니다."),

    // 쿠폰관련 에러 코드
    COUPON_ALREADY_REGISTERED("COUPON_ALREADY_REGISTERED", "이미 등록된 쿠폰입니다."),
    INVALID_COUPON("INVALID_COUPON", "유효하지 않은 쿠폰입니다."),
    COUPON_OUT_OF_STOCK("COUPON_OUT_OF_STOCK", "해당 쿠폰의 수량이 모두 소진되었습니다."),
    COUPON_EXPIRED("COUPON_EXPIRED", "해당 쿠폰은 사용 기간이 만료되었습니다."),
    COUPON_USED("COUPON_USED", "이미 사용된 쿠폰입니다."),

    //상품관련 에러 코드
    OUT_OF_STOCK("OUT_OF_STOCK", "상품 주문 수량이 재고 수량보다 많습니다."),
    DELIVERED_PRODUCT("DELIVERED_PRODUCT", "배송 완료된 상품은 취소가 불가능합니다."),

    //주문관련 에러 코드
    REQUIRED_SETTING_ADDRESS("REQUIRED_SETTING_ADDRESS", "배송지 설정이 되어있지 않습니다."),
    DONT_CREATE_ORDER("DONT_CREATE_ORDER", "상품 주문에 실패하였습니다.");

    //401

    //403

    private final String code;
    private final String msg;
}
