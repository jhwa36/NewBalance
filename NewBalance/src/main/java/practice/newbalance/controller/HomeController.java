package practice.newbalance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(){
        return "home";
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

}
