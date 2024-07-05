package practice.newbalance.service.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.domain.item.Product;
import practice.newbalance.domain.item.ProductOption;
import practice.newbalance.domain.member.Member;
import practice.newbalance.dto.item.ProductDto;
import practice.newbalance.dto.item.ProductOptionDto;
import practice.newbalance.dto.item.ProductOptionDtoDetails;
import practice.newbalance.repository.MemberRepository;
import practice.newbalance.repository.item.CartRepository;
import practice.newbalance.repository.item.ProductRepository;
import practice.newbalance.repository.item.query.CustomProductRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final FileUtils futils;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CustomProductRepository customProductRepository;

    @Transactional
    @Override
    public Map<String, Object> imgUpload(MultipartFile img) {
         String folder = "/res/img/product/imgFolder";
         String imgFolder = null;

        try {
            imgFolder = futils.transferTo(img, folder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(imgFolder == null) {
            return null;
        }
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("uploaded", 1);
        json.put("fileName", imgFolder);
        json.put("url", folder + "/" + imgFolder);
        return json;
    }

    @Override
    public Map<String, Object> getProductOption(Long productId) {
        //product option 테이블 조회
        List<ProductOptionDto> productOption = customProductRepository.getProductOption(productId);

        //컬러 중복 제거
        Set<String> colorSet = productOption.stream().map(ProductOptionDto::getColor).collect(Collectors.toSet());

        //json 데이터 option 부분 생성
        // 컬러별 사이즈 리스트를 json 형태로 반환하기 위해 세팅
        List<Map<String, Object>> option = new ArrayList<>();
        colorSet.forEach(color -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("color", color);
            List<ProductOptionDtoDetails> size = new ArrayList<>();
            productOption.stream()
                    .filter(dto -> dto.getColor().equals(color))
                    .forEach(dto -> size.add(dto.getProductOptionDtoDetailsList().get(0)));
            data.put("size", size);
            option.add(data);
        });

        //상품 정보 조회
        Product product = productRepository.findProductById(productId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );

        Map<String, Object> json = new HashMap<>();
        json.put("products", product.toDTO());
        json.put("options", option);

        return json;
    }

    @Transactional
    @Override
    public Product addProduct(ProductDto productDto) {

        ObjectMapper mapper = new ObjectMapper();
        List<ProductOptionDto> productOptionDtoList = null;

        try {
            productOptionDtoList = mapper.readValue(productDto.getOption() , new TypeReference<List<ProductOptionDto>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Product product = new Product();
        product.setCode(productDto.getCode());
        product.setContry(productDto.getContry());
        product.setFeatures(productDto.getFeatures());
        product.setManufactureDate(productDto.getManufactureDate());
        product.setMaterial(productDto.getMaterial());
        product.setTitle(productDto.getTitle());
        product.setPrice(productDto.getPrice());
        product.setContent(productDto.getContent());
        product.setImageUrls(productDto.getImageUrls());
        product.setCategory(productDto.getCategory());

        for(ProductOptionDto productOptionDto : productOptionDtoList) {
            String color = productOptionDto.getColor();

            for(ProductOptionDtoDetails optionDtoDetails : productOptionDto.getProductOptionDtoDetailsList()){
                ProductOption productOption = new ProductOption();

                productOption.setColor(color);
                productOption.setSize(optionDtoDetails.getSizeValue());
                productOption.setQuantity(optionDtoDetails.getQuantity());

                product.addOption(productOption);

            }
        }
        return productRepository.save(product);
    }

    //전체 상품 조회
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAllProducts();
    }

    //상품 상세 조회
    @Override
    public List<Product> findProductWithProductOptionsById(Long productId) {
        return productRepository.findProductWithProductOptionsById(productId);
    }

    //장바구니 상품 추가
    @Transactional
    @Override
    public void addCart(Long memberId, Long productId, String size, String color, int count) {
        //멤버 조회
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );

        //상품 조회
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );

        //option 조회 및 cart 생성
        product.getProductOptions().stream()
                .filter(p -> p.getColor().equals(color) && p.getSize().equals(size))
                .forEach(productOption -> {
                    Cart cart = Cart.createCart(member, product, productOption, product.getPrice() * count, count);
                    cartRepository.save(cart);
                });
    }

    //장바구니 상품 삭제
    @Transactional
    @Override
    public void delCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );
        cart.cancel();
        cartRepository.deleteById(cartId);
    }

    //장바구니 모든 상품 삭제
    @Transactional
    @Override
    public void delAllCart(Long memberId) {
        List<Cart> findCarts = cartRepository.findByMemberId(memberId);
        findCarts.stream().forEach(Cart::cancel);
        cartRepository.deleteByMemberId(memberId);
    }


    //장바구니 조회
    @Override
    public List<Cart> findCartAll(Long memberId) {
        return cartRepository.findByMemberId(memberId);
    }

    //장바구니 상품 옵션 변경
    @Transactional
    @Override
    public void updateCartOption(Long cartId, String size, String color) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );

        cart.getProduct().getProductOptions().stream()
                .filter(p -> p.getColor().equals(color) && p.getSize().equals(size))
                .forEach(
                        productOption -> cart.updateOption(cart, productOption)
                );

    }

    //장바구니 상품 수량 변경
    @Transactional
    @Override
    public void updateCartCount(Long cartId, int count) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA)
        );
        cart.updateCount(cart, count);
    }
}

