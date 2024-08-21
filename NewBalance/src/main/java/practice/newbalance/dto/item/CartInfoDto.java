package practice.newbalance.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartInfoDto {
    private Long cartId;
    private String title;
    private String color;
    private String size;
    private int count;
    private int price;
}
