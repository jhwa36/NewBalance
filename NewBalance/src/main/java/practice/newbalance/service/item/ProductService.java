package practice.newbalance.service.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.domain.item.Product;
import practice.newbalance.dto.item.ProductDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductService {

    Map<String, Object>imgUpload(MultipartFile img);

    //product
    Product addProduct(ProductDto productDto, List<MultipartFile> thumbnails) throws IOException;
    Product updateProduct(Long productId, ProductDto productDto, List<ThumbnailDto> existingThumbnails, List<MultipartFile> newThumbnails) throws IOException;
    List<Product> getAllProducts();
    List<Product> findProductWithProductOptionsById(Long productId);
    Map<String, Object> getProductOption(Long productId);

    Optional<Product> findProductById(Long id);

    List<String> getSizeValues(Long productId);

    void deleteByColorAndIdIn(String color, List<Integer> optionId);
    void deleteByProductId(Long productId);

    void deleteByThumbnailId(Long thumbnailId);

    //cart
    void addCart(Long memberId, Long productId, String size, String color, int count);
    void delCart(Long cartId);
    void delAllCart(Long memberId);
    List<Cart> findCartAll(Long memberId);
    void updateCartOption(Long cartId, String size, String color);
    void updateCartCount(Long cartId, int count);

    Page<ProductDto> getProductsByCategoryId(Long categoryId, List<String> sizes, List<String> colors, Integer minPrice,
                                             Integer maxPrice,  Pageable pageable);
    Page<ProductDto> getProductsByKeyword(String keyword, List<String> sizes, List<String> colors, Integer minPrice,
                                          Integer maxPrice,  Pageable pageable);
}
