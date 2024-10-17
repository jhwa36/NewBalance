package practice.newbalance.dto.item;

import lombok.Data;

import java.util.List;

@Data
public class ProductFilterDto {
    private String categoryId;
    private List<String> sizes;
    private List<String> colors;
    private Integer minPrice;
    private Integer maxPrice;
}
