package practice.newbalance.domain.item;

import lombok.Getter;

@Getter
public enum CouponEnum {
    NEW("NEW"),
    NOT_USED("NOT_USED"), // 사용자 쿠폰 등록
    USED("USED"),   //사용한상태
    EXPIRED("EXPIRED"); //기간만료

    private final String status;

    CouponEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
