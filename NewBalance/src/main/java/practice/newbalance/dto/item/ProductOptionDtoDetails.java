package practice.newbalance.dto.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class ProductOptionDtoDetails {
    private Long id;
    private String sizeValue;
    private int quantity;
}
