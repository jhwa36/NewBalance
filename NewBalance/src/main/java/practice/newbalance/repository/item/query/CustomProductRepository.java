package practice.newbalance.repository.item.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import practice.newbalance.dto.item.ProductDto;
import practice.newbalance.dto.item.ProductOptionDto;


import java.util.List;

public interface CustomProductRepository {
    public List<ProductDto> getProductDetail(Long productId);

    List<ProductOptionDto> getProductOption(Long productId);

    Page<ProductDto> findProductsByCategoryId(Long categoryId, List<String> sizes, List<String> colors, Integer minPrice, Integer maxPrice,  Pageable pageable);
    Page<ProductDto> findProductByKeyword(String keyword, List<String> sizes, List<String> color, Integer minPrice,
                                          Integer maxPrice, Pageable pageable);
}
