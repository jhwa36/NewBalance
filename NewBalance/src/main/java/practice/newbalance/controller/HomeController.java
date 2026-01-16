package practice.newbalance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.item.Product;
import practice.newbalance.dto.item.ProductDto;
import practice.newbalance.dto.member.MemberDto;
import practice.newbalance.repository.MemberMapper;
import practice.newbalance.service.MemberService;
import practice.newbalance.service.item.ProductService;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final MemberService memberService;
    public HomeController(ProductService productService, MemberService memberService) {
        this.productService = productService;
        this.memberService = memberService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) List<String> sizes,
                       @RequestParam(required = false) List<String> colors,
                       @RequestParam(required = false) Integer minPrice,
                       @RequestParam(required = false) Integer maxPrice,
                       @PageableDefault(size = 12) Pageable pageable,
                       Model model) {
//        List<Product> products = productService.getAllProducts();
//        model.addAttribute("products", products);

        Page<ProductDto> productPage =
                productService.findProductsByCategory(
                        categoryId, sizes, colors, minPrice, maxPrice, pageable
                );

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);

        List<MemberDto> members = memberService.getAllMembers();
        model.addAttribute("members", members);
        return "home";
    }
    // Mybatis findOne
    @GetMapping("/member")
    public String memberByEmail(@RequestParam String email, Model model){
            MemberDto member = memberService.getMemberByEmail(email);
            model.addAttribute("member",member);
            return "memberDetail";
    }



    // 상품상세조회
    @GetMapping("{id}")
    public String viewPort(@PathVariable Long id, Model model) {
        Product product = productService.findProductById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA));
        model.addAttribute("product", product);
        return "item/productDetail";
    }
}




    //테스트용
//    @GetMapping("test/bad-request")
//    public ResponseEntity<Object> test400Error() {
//        try{
//            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA);
//        }
//        catch(Exception ex) {
//            throw new CustomException(ex);
//        }
//    }

