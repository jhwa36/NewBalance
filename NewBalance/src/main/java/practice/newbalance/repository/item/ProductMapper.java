package practice.newbalance.repository.item;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import practice.newbalance.dto.item.ProductOptionRowDto;

import java.util.List;

@Mapper
public interface ProductMapper {

    long countProductsByCategory(
            @Param("categoryId") Long categoryId,
            @Param("sizes") List<String> sizes,
            @Param("colors") List<String> colors,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice
    );

    List<ProductOptionRowDto> findProductsByCategory(
            @Param("categoryId") Long categoryId,
            @Param("sizes") List<String> sizes,
            @Param("colors") List<String> colors,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
