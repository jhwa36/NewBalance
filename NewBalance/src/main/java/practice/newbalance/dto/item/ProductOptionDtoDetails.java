package practice.newbalance.dto.item;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class ProductOptionDtoDetails {
    private Long id;
    private String sizeValue;
    private Integer quantity;

    @QueryProjection
    public ProductOptionDtoDetails(String size, Integer quantity){
        this.sizeValue = size;
        this.quantity = quantity;
    }
}
