package practice.newbalance.dto.item;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import practice.newbalance.domain.item.Category;
import practice.newbalance.domain.item.Product;
import practice.newbalance.domain.item.Thumbnail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@Setter @Getter
@NoArgsConstructor
public class ProductDto {

    private Long id;

    private String title;

    private String content;

    private List<ProductOptionDto> productOptions = new ArrayList<>();

    private String code;

    private String contry;

    private String material;

    private String features;

    private int price;

    private LocalDate manufactureDate;

    private Category category;

    private String option;

    private List<String> imageUrls = new ArrayList<>();

    private List<String> thumbnailUrl = new ArrayList<>();  // 썸네일 URL 리스트 추가

    @QueryProjection
    public ProductDto(Long id, String title, String content, String code,
                      String contry, String material, String features, int price,
                      LocalDate manufactureDate, Category category, List<ProductOptionDto> productOptions){
        this.id = id;
        this.title = title;
        this.content = content;
        this.code = code;
        this.contry = contry;
        this.material = material;
        this.features = features;
        this.price = price;
        this.manufactureDate = manufactureDate;
        this.category = category;
        this.productOptions = productOptions;
    }

    @QueryProjection
    public ProductDto(Long id, String title, String content, String code, String contry,
                      String material, String features, int price, LocalDate manufactureDate,
                      Category category, List<ProductOptionDto> productOptions, List<String> thumbnailUrls) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.code = code;
        this.contry = contry;
        this.material = material;
        this.features = features;
        this.price = price;
        this.manufactureDate = manufactureDate;
        this.category = category;
        this.productOptions = productOptions;
        this.thumbnailUrl = thumbnailUrls;  // 추가된 부분
    }
    // 생성자
    public ProductDto(Long id, String title, String content, String code, String contry, String material, String features, Integer price, LocalDate manufactureDate, Category category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.code = code;
        this.contry = contry;
        this.material = material;
        this.features = features;
        this.price = price;
        this.manufactureDate = manufactureDate;
        this.category = category;
    }
    public Product toEntity(){
        Product product = Product.builder()
                .id(id)
                .title(title)
                .content(content)
                .code(code)
                .contry(contry)
                .material(material)
                .features(features)
                .price(price)
                .manufactureDate(manufactureDate)
                .category(category)
                .imageUrls(imageUrls)
                .build();

        List<Thumbnail> thumbnails = this.thumbnailUrl.stream()
                .map(url -> new Thumbnail(url, product))
                .collect(Collectors.toList());

        product.setThumbnailUrl(thumbnails);
        return product;
    }
}
