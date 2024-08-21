package practice.newbalance.domain.item;

public enum OrderStatus {
    PAYMENT("결제"), SHIPPING("배송준비중/배송중"),
    COMPLETE("배송완료"), CONFIRMATION("구매확정"),
    CANCEL("주문취소");

    private String value;

    OrderStatus(String value) {
        this.value = value;
    }
}
