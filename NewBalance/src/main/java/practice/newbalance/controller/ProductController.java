package practice.newbalance.controller;


import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import practice.newbalance.domain.item.Product;
import practice.newbalance.dto.item.CategoryDto;
import practice.newbalance.dto.item.ProductDto;
import practice.newbalance.service.item.CategoryService;
import practice.newbalance.dto.item.ProductOptionDto;
import practice.newbalance.service.item.ProductService;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.ui.Model;
import practice.newbalance.config.security.CustomUserDetail;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.dto.item.CartDto;
import practice.newbalance.service.item.ThumbnailDto;


@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    //이미지 업로드
    @PostMapping("/image/upload")
    @ResponseBody
    public Map<String, Object> imgUpload(@RequestParam("upload")MultipartFile file){
        return productService.imgUpload(file);
    }

    //상품 카테고리 전체 조회
    @GetMapping("/products/categories")
    public ResponseEntity<Map<String, Object>> categoryList(){
        Map<String, Object> response = new HashMap<>();
        List<CategoryDto> categories = categoryService.getAllCategories();
        response.put("categories", categories);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    //상품 카테고리 상세 조회
    @GetMapping("/products/subCategories")
    public ResponseEntity<Map<String, Object>> categoryList(@RequestParam(value = "title", required = false) String title){
        Map<String, Object> response = new HashMap<>();
        List<CategoryDto> categoryDtos = categoryService.findByCategory(title);
        response.put("categoryDtos", categoryDtos);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    //상품 카테고리 조회
    @GetMapping("/products/detailedCategories")
    public ResponseEntity<Map<String, Object>> getDetailCategorries(
            @RequestParam(value = "parentTitle", required = false) String parentTitle,
            @RequestParam(value = "subCategoryRef", required = false) Integer subCategoryRef){
        Map<String, Object> response = new HashMap<>();
        List<CategoryDto> detailedCategories  = categoryService.findDetailedCategories(parentTitle, subCategoryRef);
        response.put("detailedCategories", detailedCategories);
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/thumbnails/{thumbnailId}")
    public ResponseEntity<String> deleteThumbnail(@PathVariable(value = "thumbnailId") Long thumbnailId) {
        try {
            productService.deleteByThumbnailId(thumbnailId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * Product 등록
     * @param productDto
     * @return
     */
    @PostMapping("/products/addProduct")
    public ResponseEntity<String> addItem(@RequestPart("productDto") ProductDto productDto,
                                          @RequestPart("thumbnails") List<MultipartFile> thumbnails) {
        try{
            productService.addProduct(productDto,thumbnails);
            return ResponseEntity.ok("success");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * Product 수정
     * @param productId
     * @param productDto
     * @return
     */
    @PutMapping( "/products/updateProduct/{productId}")
    public ResponseEntity<String> updateItem(
            @PathVariable(value = "productId") Long productId,
            @RequestPart("productDto") ProductDto productDto,
            @RequestPart(value = "existingThumbnails", required = false) List<ThumbnailDto> existingThumbnails, // 기존 썸네일
            @RequestPart(value = "thumbnails", required = false) List<MultipartFile> thumbnails // 추가된 썸네일
            ) {
        try {
            productService.updateProduct(productId, productDto, existingThumbnails, thumbnails);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * Product Option 삭제
     */
    @PostMapping("/products/deleteOption")
    public ResponseEntity<String> deleteProductOption(@RequestBody Map<String, Object> option){
        String color = (String) option.get("color");
        List<Integer> optionId = (List<Integer>) option.get("optionId");
            productService.deleteByColorAndIdIn(color, optionId);
            return ResponseEntity.ok("success");
    }

    @DeleteMapping("/products/deleteProduct/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("productId") Long productId){
        productService.deleteByProductId(productId);
        return ResponseEntity.ok("success");
    }

    /**
     * 상품 전체 조회
     * @return
     */
    @GetMapping("/products/productList")
    public ResponseEntity<List<Product>> getProductList(){
        List<Product> productList = productService.getAllProducts();
        return ResponseEntity.ok().body(productList);
    }

    /**
     * 상품 옵션 상세조회
     * @param productId
     * @return
     */
    @GetMapping("/products/{productId}")
    @ResponseBody
    public Map<String, Object> getProductOption(@PathVariable("productId") Long productId){
        return productService.getProductOption(productId);
    }

    /**
     * 상품 상세 조회
     * @param productId
     * @return
     */
    @GetMapping("/products/getProductDetails")
    public ResponseEntity<Map<String,Object>> getProductDetails(@RequestParam(value = "productId") Long productId){
        List<Product> productDetails = productService.findProductWithProductOptionsById(productId);
        List<CategoryDto> categories = categoryService.getAllCategories();
        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        response.put("productDetails", productDetails);
        return ResponseEntity.ok().body(response);
    }

    //장바구니 홈 이동
    @GetMapping("/my/cart")
    public String cartHome(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            Model model
    ){
        List<Cart> cartAll = productService.findCartAll(customUserDetail.getMember().getId());
        List<String> countList = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            countList.add(String.valueOf(i+1));
        }
        model.addAttribute("cartList", cartAll);
        model.addAttribute("countList", countList);
        return "item/cart";
    }

    /**
     * 장바구니 추가
     * @param customUserDetail
     * @param dto
     * @return
     */
    @PostMapping("/my/cart")
    public ResponseEntity<String> addCart(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            CartDto dto
    ){
        productService.addCart(
                customUserDetail.getMember().getId(), dto.getProductId(),
                dto.getSize(), dto.getColor(), dto.getCount()
        );
        return ResponseEntity.ok("success");
    }

    /**
     * 장바구니 상품 옵션 변경
     * @param customUserDetail
     * @param cartId
     * @param dto
     * @return
     */
    @PutMapping("/my/cart/option/{cartId}")
    public ResponseEntity<String> updateCart(
            @AuthenticationPrincipal CustomUserDetail customUserDetail,
            @PathVariable("cartId") Long cartId,
            CartDto dto
    ){
        productService.updateCartOption(cartId, dto.getSize(), dto.getColor());
        return ResponseEntity.ok("success");
    }

    /**
     * 장바구니 상품 수량 변경
     * @param cartId
     * @param count
     * @return
     */
    @PutMapping("/my/cart/count/{cartId}")
    public ResponseEntity<String> updateCartCount(@PathVariable("cartId") Long cartId, @RequestParam("count") int count){
        productService.updateCartCount(cartId, count);
        return ResponseEntity.ok("success");
    }

    /**
     * 장바구니 상품 삭제
     * @param cartId
     * @return
     */
    @DeleteMapping("/my/cart/{cartId}")
    public ResponseEntity<String> delCart(@PathVariable("cartId") Long cartId){
        productService.delCart(cartId);
        return ResponseEntity.ok("success");
    }

    /**
     * 장바구니 상품 전체 삭제
     * @param customUserDetail
     * @return
     */
    @DeleteMapping("/my/cart")
    public ResponseEntity<String> delAllCart(@AuthenticationPrincipal CustomUserDetail customUserDetail){
        productService.delAllCart(customUserDetail.getMember().getId());
        return ResponseEntity.ok("success");
    }
}
