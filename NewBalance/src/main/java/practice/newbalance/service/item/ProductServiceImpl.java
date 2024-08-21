package practice.newbalance.service.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.domain.item.Product;
import practice.newbalance.domain.item.ProductOption;
import practice.newbalance.domain.item.Thumbnail;
import practice.newbalance.domain.member.Member;
import practice.newbalance.dto.item.ProductDto;
import practice.newbalance.dto.item.ProductOptionDto;
import practice.newbalance.dto.item.ProductOptionDtoDetails;
import practice.newbalance.repository.MemberRepository;
import practice.newbalance.repository.item.CartRepository;
import practice.newbalance.repository.item.ProductOptionRepository;
import practice.newbalance.repository.item.ProductRepository;
import practice.newbalance.repository.item.ThumbnailRepository;
import practice.newbalance.repository.item.query.CustomProductRepository;

import java.io.IOException;
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
    private final ProductOptionRepository productOptionRepository;
    private final ThumbnailRepository thumbnailRepository;

    @Value("${server.url}")
    private String serverUrl; // 서버 URL을 프로퍼티 파일에서 읽어옴

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

    private List<String> saveThumbnails(List<MultipartFile> thumbnails) throws IOException {
        List<String> thumbnailUrls = new ArrayList<>();
        String folder = "/res/img/product/thumbnails"; // 썸네일이 저장될 폴더 경로

        for (MultipartFile file : thumbnails) {
            try {
                String fileName = futils.transferToThumbnail(file, folder);
                thumbnailUrls.add(serverUrl + folder + "/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Failed to save thumbnail", e);
            }
        }
        return thumbnailUrls;
    }

    @Transactional
    @Override
    public void deleteByThumbnailId(Long thumbnailId) {
        thumbnailRepository.deleteById(thumbnailId);
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
    public Product addProduct(ProductDto productDto, List<MultipartFile> thumbnails) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        List<ProductOptionDto> productOptionDtoList = null;

        try {
            productOptionDtoList = mapper.readValue(productDto.getOption() , new TypeReference<List<ProductOptionDto>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        List<String> thumbnailUrls = saveThumbnails(thumbnails);
        productDto.setThumbnailUrl(thumbnailUrls); // ProductDto에 썸네일 URL 추가

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

        for(String url : thumbnailUrls) {
            Thumbnail thumbnail = new Thumbnail(url, product);
            product.addThumbnail(thumbnail);
        }

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

    @Transactional
    @Override
    public Product updateProduct(Long productId, ProductDto productDto, List<ThumbnailDto> existingThumbnails, List<MultipartFile> newThumbnails) throws IOException{
        // 기존 Product 불러오기
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA));

        // ObjectMapper 초기화
        ObjectMapper mapper = new ObjectMapper();
        List<ProductOptionDto> productOptionDtoList = null;

        try {
            // ProductDto에서 Option 정보 읽어오기
            productOptionDtoList = mapper.readValue(productDto.getOption(), new TypeReference<List<ProductOptionDto>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // JSON 변환 오류 처리
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.UNKNOWN);
        }

        // 기존 Product 정보 업데이트
        product.setTitle(productDto.getTitle());
        product.setPrice(productDto.getPrice());
        product.setCode(productDto.getCode());
        product.setContry(productDto.getContry());
        product.setManufactureDate(productDto.getManufactureDate());
        product.setMaterial(productDto.getMaterial());
        product.setFeatures(productDto.getFeatures());
        product.setCategory(productDto.getCategory());
        product.setContent(productDto.getContent());
        product.setImageUrls(productDto.getImageUrls());

        // 기존 썸네일 업데이트
        for (ThumbnailDto dto : existingThumbnails) {
            Thumbnail thumbnail = product.getThumbnailUrl().stream()
                    .filter(t -> t.getId().equals(dto.getId()))
                    .findFirst()
                    .orElse(null);
            if(thumbnail != null) {
                thumbnail.updateUrl(dto.getThumbnailUrl());
                thumbnailRepository.save(thumbnail);
            }
        }

        // 새 썸네일 저장
        if(newThumbnails != null && !newThumbnails.isEmpty()) {
            List<String> newThumbnailUrls = saveThumbnails(newThumbnails);
            for(String url : newThumbnailUrls) {
                Thumbnail newThumbnail = new Thumbnail(url, product);
                product.addThumbnail(newThumbnail);

            }
        } else {
            // 새 썸네일이 없을 경우 기존 썸네일 유지
        }

        // 기존 ProductOptions 삭제
        product.getProductOptions().clear();

        // 새로운 ProductOptions 추가 또는 업데이트
        for (ProductOptionDto productOptionDto : productOptionDtoList) {
            String color = productOptionDto.getColor();

            for (ProductOptionDtoDetails productOptionDtoDetails : productOptionDto.getProductOptionDtoDetailsList()) {
                Long optionId = productOptionDtoDetails.getId();
                ProductOption productOption;

                if (optionId != null) {
                    // 기존 ProductOption 업데이트
                    productOption = productOptionRepository.findById(optionId)
                            .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA));

                    productOption.setColor(color);
                    productOption.setSize(productOptionDtoDetails.getSizeValue());
                    productOption.setQuantity(productOptionDtoDetails.getQuantity());
                } else {
                    // 새로운 ProductOption 추가
                    productOption = new ProductOption();
                    productOption.setColor(color);
                    productOption.setSize(productOptionDtoDetails.getSizeValue());
                    productOption.setQuantity(productOptionDtoDetails.getQuantity());
                }

                // Product에 ProductOption 추가
                product.addOption(productOption);
            }
        }

        // Product 저장 및 반환
        return productRepository.save(product);
    }

    @Transactional
    @Override
    public void deleteByColorAndIdIn(String color, List<Integer> optionId) {
            productOptionRepository.deleteByColorAndIdIn(color, optionId);
    }

    @Transactional
    @Override
    public void deleteByProductId(Long productId) {
        productRepository.deleteById(productId);
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
        cartRepository.deleteById(cartId);
    }

    //장바구니 모든 상품 삭제
    @Transactional
    @Override
    public void delAllCart(Long memberId) {
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
        cart.setCount(count);
        cart.setPrice(cart.getProduct().getPrice() * count);
    }
}

