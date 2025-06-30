package practice.newbalance.controller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import practice.newbalance.domain.board.FaqTag;
import practice.newbalance.domain.board.Notice;
import practice.newbalance.dto.board.FaqDto;
import practice.newbalance.dto.board.NoticeDto;
import practice.newbalance.dto.item.CategoryDto;
import practice.newbalance.dto.item.CouponDto;
import practice.newbalance.dto.member.MemberDto;
import practice.newbalance.service.MemberService;
import practice.newbalance.service.board.FaqServiceImpl;
import practice.newbalance.service.board.NoticeService;
import practice.newbalance.service.item.CategoryService;
import practice.newbalance.service.item.CouponService;
import practice.newbalance.service.item.ProductService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminController {
    private final NoticeService noticeService;
    private final MemberService memberService;
    private final FaqServiceImpl faqService;
    private final CategoryService categoryService;
    private  final CouponService couponService;

    private  final ProductService productService;
    @Autowired
    public AdminController(NoticeService noticeService, MemberService memberService, FaqServiceImpl faqService, CategoryService categoryService, CouponService couponService, ProductService productService) {
        this.noticeService = noticeService;
        this.memberService = memberService;
        this.faqService = faqService;
        this.categoryService = categoryService;
        this.couponService = couponService;
        this.productService = productService;
    }

    @GetMapping("/admin-page")
    public String adminPage(){

        return "admin/adminPage";
    }


    /**
     * 공지사항 list출력
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping(value = "/admin/notices")
    @ResponseBody
    public Map<String, Object> adminNoticePage(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "10") int limit){

        List<NoticeDto> notices = noticeService.getNotice(offset, limit);
        long totalNotices = noticeService.getNoticeCount();

        Map<String, Object> response = new HashMap<>();

        response.put("notices", notices);
        response.put("totalNotices", totalNotices);

        return response;
    }

    @GetMapping("/admin/faqsList")
    @ResponseBody
    public Map<String, Object> FaqList(
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        //todo: 설정 값으로 대체 예정
        int limit = 3;
        boolean isSearch = tag != null;

        Page<FaqDto> faqList = isSearch ?
                faqService.findAll(page, limit, condition, tag) :
                faqService.findAll(page, limit);

        long dataCnt = (
                isSearch ?
                        faqService.getSearchCount(condition, tag) - ((long) (page + 1) * limit) :
                        faqService.getFaqCount() - ((long) (page + 1) * limit)
        );

        Map<String, Object> response = new HashMap<>();

        response.put("contents", faqList.getContent());
        response.put("page", page);
        response.put("count", dataCnt <= 0 ? 0 : dataCnt);
        response.put("tag", tag);
        response.put("condition", condition);

        return response;
    }

    @GetMapping("/admin/api/faqs")
    @ResponseBody
    public Map<String, Object> getContents(
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        //todo: 설정 값으로 대체 예정
        int limit = 3;
        boolean isSearch = tag != null;
        Page<FaqDto> faqList = isSearch ?
                faqService.findAll(page, limit, condition, tag) :
                faqService.findAll(page, limit);

        long dataCnt = (
                isSearch ?
                        faqService.getSearchCount(condition, tag) - ((long) (page + 1) * limit) :
                        faqService.getFaqCount() - ((long) (page + 1) * limit)
        );


        Map<String, Object> data = new HashMap<>();
        data.put("contents", faqList.getContent());
        data.put("page", page);
        data.put("count", dataCnt <= 0 ? 0 : dataCnt);
        data.put("tag", tag);
        data.put("condition", condition);

        return data;
    }

    /**
     * faq Enum 데이터 SelectBox로 로드
     * @return
     */
    @GetMapping("/admin/faqTagList")
    @ResponseBody
    public List<Map<String, String>> FaqTagController(){
        return Arrays.stream(FaqTag.values())
                .map(faqTag -> Map.of("value", faqTag.name(), "tagName", faqTag.getTagName()))
                .collect(Collectors.toList());
    }

    /**
     * faq 등록
     */
    @PostMapping("/admin/faqEdit")
    public ResponseEntity<String> createFaq(@RequestBody FaqDto faqDto) {
        faqService.saveFaq(faqDto);
        return ResponseEntity.ok("등록완료");
    }


    /**
     * admin페이지 faq modal페이지에서 글 수정
     * @param faqId
     * @return
     */
    @PutMapping(value = "/admin/faqEdit/{faqId}")
    public ResponseEntity<String> updateFaq(@PathVariable("faqId") Long faqId,
                                               @RequestBody FaqDto faqDto) {
        try{
            faqService.updateFaq(faqId, faqDto);
            return ResponseEntity.ok("success");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * admin페이지 faq modal페이지에서 글 삭제
     * @param faqId
     * @return
     */
    @DeleteMapping(value = "/admin/faqDelete/{faqId}")
    public ResponseEntity<String> deleteFaq(@PathVariable("faqId") Long faqId){
        try{
            faqService.deleteFaq(faqId);
            return ResponseEntity.ok("success");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * 공지사항 상세 폼
     */
    @GetMapping(value = "/admin/notice-detail/{noticeId}")
    public String detailNoticeForm(@PathVariable("noticeId") Long noticeId,
                                   Model model) {

        Notice noticeDto = noticeService.findNoticeById(noticeId);
        model.addAttribute("noticeDto", noticeDto);

        return "board/noticeDetail";
    }

    /**
     * admin페이지 공지사항 modal페이지에서 글 수정
     * @param noticeId
     * @return
     */
    @PostMapping(value = "/admin/noticeEdit/{noticeId}")
    public ResponseEntity<String> updateNotice(@PathVariable("noticeId") Long noticeId,
                                               @RequestBody NoticeDto noticeDto) {
        try{
            noticeService.updateNotice(noticeId, noticeDto);
            return ResponseEntity.ok("success");
        }catch (Exception e){
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * admin페이지 공지사항 modal페이지에서 글 삭제
     * @param noticeId
     * @return
     */
    @DeleteMapping(value = "/admin/noticeDelete/{noticeId}")
    public ResponseEntity<String> deleteNotice(@PathVariable("noticeId") Long noticeId){
        try{
            noticeService.deleteNotice(noticeId);
            return ResponseEntity.ok("success");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * 회원list
     */
    @GetMapping("/admin/membersList")
    @ResponseBody
    public Map<String, Object> membersList(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit){

        List<MemberDto> members = memberService.findMemberAll(offset, limit);
        long totalMembers = memberService.getMemberCount();

        Map<String, Object> response = new HashMap<>();
        response.put("members", members);
        response.put("totalMembers", totalMembers);

        return response;
    }

    /**
     * 카테고리list
     */
    @GetMapping("/admin/categoryList")
    @ResponseBody
    public Map<String, Object> categoryList(
            @RequestParam(value = "title", required = false) String title){

        List<CategoryDto> categoryDtos = categoryService.findByCategory(title);

        Map<String, Object> response = new HashMap<>();
        response.put("categoryDtos", categoryDtos);

        return response;
    }

    /**
     * 카테고리 메뉴 추가
     */
    @PostMapping("/admin/addItem")
    public ResponseEntity<String> addItem(@RequestBody CategoryDto categoryDto) {
        try{
            categoryService.addItem(categoryDto);
            return ResponseEntity.ok("success");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * 카테고리 메뉴 수정
     */
    @PostMapping(value = "/admin/editItem/{categoryId}")
    public ResponseEntity<String> editItem(@PathVariable("categoryId") Long categoryId,
                                           @RequestBody CategoryDto categoryDto) {
        try {
            categoryService.editItem(categoryId, categoryDto);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    /**
     * 카테고리 메뉴 삭제
     */
    @PostMapping("/admin/deleteItem/{categoryId}")
    public ResponseEntity<String> deleteItem(@PathVariable("categoryId") Long categoryId) {
        try{
            categoryService.deleteItem(categoryId);
            return ResponseEntity.ok("success");
        }catch (Exception e){
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * 쿠폰list
     * @return
     */
    @GetMapping(value = "/admin/coupons")
    @ResponseBody
    public Map<String, Object> couponList(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", defaultValue = "20") int limit){

        List<CouponDto> coupons =  couponService.findCouponAll(offset, limit);
        long totalCoupon = couponService.getCouponCount();

        Map<String, Object> respons = new HashMap<>();

        respons.put("coupons", coupons);
        respons.put("totalCoupon", totalCoupon);

        return respons;
    }

    @PostMapping("/admin/addCoupon")
    public ResponseEntity<String> addCoupon(@RequestBody CouponDto couponDto){
        try {
            couponService.addCoupon(couponDto);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }
    @PutMapping("/admin/updateCoupon/{couponId}")
    public ResponseEntity<String> updateCoupon(@PathVariable("couponId") Long couponId,
                                               @RequestBody CouponDto couponDto) {
        try {
            couponService.updateCoupon(couponId, couponDto);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    @DeleteMapping("/admin/deleteCoupon/{couponId}")
    public ResponseEntity<String> deleteCoupon(@PathVariable("couponId") Long couponId){
        try {
            couponService.deleteCoupon(couponId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }
}
