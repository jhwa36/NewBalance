package practice.newbalance.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import practice.newbalance.domain.item.CategoryEnum;
import practice.newbalance.domain.item.Product;
import practice.newbalance.dto.item.*;
import practice.newbalance.service.item.CategoryService;
import practice.newbalance.service.item.ProductService;

import java.util.*;
import java.util.stream.Collectors;

import practice.newbalance.config.security.CustomUserDetail;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.service.item.ThumbnailDto;


@Controller
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
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

    /**
     * 상품 카테고리 상세 페이지 이동
     * @param categoryId
     * @param model
     * @return
     */
    @GetMapping("/categories/{categoryId}")
    public String productList(@PathVariable("categoryId")Long categoryId,
                              @RequestParam(value = "sizes", required = false) List<String> sizes,
                              @RequestParam(value = "colors", required = false) List<String> colors,
                              @RequestParam(value = "minPrice", required = false) Integer minPrice,
                              @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
                              Pageable pageable, Model model){

        int checkMinPrice = (minPrice != null) ? minPrice : 0;
        int checkMaxPrice = (maxPrice != null) ? maxPrice : Integer.MAX_VALUE;

        Page<ProductDto> products = productService.getProductsByCategoryId(categoryId, sizes, colors, checkMinPrice, checkMaxPrice, pageable);

        // 사이즈
        Set<String> size = products.stream()
                .flatMap(product -> product.getProductOptions().stream()) // 1차 평탄화 Stream<ProductOption>
                .flatMap(option-> option.getProductOptionDtoDetailsList().stream()) // 2차 평탄화 Stream<ProductOptionDtoDetails>
                .map(ProductOptionDtoDetails::getSizeValue) //사이즈 값 추출
                .collect(Collectors.toSet()); // Set으로 수집하여 중복 제거

        int productMaxPrice = products.getContent().stream()
                .mapToInt(ProductDto::getPrice)
                .max()
                .orElse(0);

        // 색상
        Set<String> color = products.stream()
                .flatMap(product -> product.getProductOptions().stream()) // ProductOptionDto 리스트 평탄화
                .map(ProductOptionDto::getColor) // ProductOptionDto의 getColor()메서드 호출
                .collect(Collectors.toSet()); // Set으로 수집하여 중복 제거

        // 카테고리(Men, Women, Kids) Enum
        CategoryEnum categoryTitle = products.stream()
                .map(product -> product.getCategory().getTitle())
                .findFirst()
                .orElse(null);

        // 카테고리(1~7) ref
        Integer categoryRef = products.stream()
                .map(product -> product.getCategory().getRef())
                .findFirst()
                .orElse(null);

        // 카테고리(Men, Women, Kids, 1~7)
        List<CategoryDto> categorys = categoryService.findByTitleAndRef(categoryTitle, categoryRef);


        model.addAttribute("products", products);
        model.addAttribute("color", color);
        model.addAttribute("size", size);
        model.addAttribute("categorys", categorys);
        model.addAttribute("maxPrice", productMaxPrice);

        return "/item/productsList";
    }

    @PostMapping("/categories/productFilter")
    public ResponseEntity<Map<String, Object>> getFilterProducts(@RequestBody ProductFilterDto filter, Pageable pageable) {

        Long categoryId = Long.parseLong(filter.getCategoryId());
        List<String> sizes = filter.getSizes();
        List<String> colors = filter.getColors();
        Integer minPrice = filter.getMinPrice();
        Integer maxPrice = filter.getMaxPrice();

        Page<ProductDto> productDtoPage = productService.getProductsByCategoryId(categoryId, sizes, colors, minPrice, maxPrice, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", productDtoPage);

        return ResponseEntity.ok(response);
    }

    //검색 관련 controller
    @GetMapping("/search")
    public String searchHome(
            @RequestParam("keyword") String keyword,
            @RequestParam("option") String option,
            @RequestParam(value = "sizes", required = false) List<String> sizes,
            @RequestParam(value = "colors", required = false) List<String> colors,
            @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            Pageable pageable, Model model){

        log.info("keyword = {}, option = {}", keyword, option);

        Map<Integer, List<CategoryDto>> menCategories = categoryService.getGroupedCategoriesByTitle(CategoryEnum.MEN);
        Map<Integer, List<CategoryDto>> womenCategories = categoryService.getGroupedCategoriesByTitle(CategoryEnum.WOMEN);
        List<String> genderList = Arrays.asList(new String[]{"MEN", "WOMEN", "KIDS"});
        List<String> priceList = Arrays.asList(
                new String[]{"5만원 미만", "5만원 - 10만원 미만", "10만원 - 15만원 미만",
                        "15만원 - 20만원 미만", "20만원 이상"});
        List<String> categoryList = Arrays.asList(new String[]{"신발", "러닝", "의류"});

        Page<ProductDto> products = productService.getProductsByKeyword(keyword, sizes, colors, minPrice, maxPrice, pageable);
        //컬러 리스트 저장
        //사이즈 리스트 저장

        model.addAttribute("count", 0);
        model.addAttribute("keyword", keyword);
        model.addAttribute("option", option);

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("genderList", genderList);
        model.addAttribute("priceList", priceList);
        model.addAttribute("products", products);
        model.addAttribute("colors", colors);
        model.addAttribute("sizes", sizes);


        return "/item/searchResult";

    }
}
