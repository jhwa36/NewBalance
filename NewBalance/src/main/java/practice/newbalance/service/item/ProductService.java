package practice.newbalance.service.item;

import org.springframework.web.multipart.MultipartFile;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.domain.item.Product;
import practice.newbalance.dto.item.ProductDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProductService {

    Map<String, Object>imgUpload(MultipartFile img);

    //product
    Product addProduct(ProductDto productDto, List<MultipartFile> thumbnails) throws IOException;
    Product updateProduct(Long productId, ProductDto productDto, List<ThumbnailDto> existingThumbnails, List<MultipartFile> newThumbnails) throws IOException;
    List<Product> getAllProducts();
    List<Product> findProductWithProductOptionsById(Long productId);
    Map<String, Object> getProductOption(Long productId);

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

    List<Product> getProductsByCategoryId(Long categoryId);
}
