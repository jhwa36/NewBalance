package practice.newbalance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;
import practice.newbalance.domain.item.Product;
import practice.newbalance.service.item.ProductService;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "home";
}

    // 상품상세조회
//    @GetMapping("/product/{id}")
//    public String viewPort(@PathVariable Long id, Model model) {
//        Product product = productService.findProductById(id)
//                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_DATA));
//        model.addAttribute("product", product);
//        return "item/productDetail";
//    }
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

