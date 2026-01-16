package practice.newbalance.dto.item;

import lombok.Data;
import practice.newbalance.domain.item.Category;

import java.time.LocalDate;

@Data
public class ProductOptionRowDto {
    private Long productId;
    private String title;
    private String content;
    private String code;
    private String contry;
    private String material;
    private String features;
    private Integer price;
    private LocalDate manufactureDate;
    private Category category;

    private String color;
    private String size;
    private Integer quantity;

}
