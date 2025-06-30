package practice.newbalance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import practice.newbalance.domain.board.Notice;
import practice.newbalance.dto.board.FaqDto;
import practice.newbalance.dto.board.NoticeDto;
import practice.newbalance.service.board.FaqServiceImpl;

import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import practice.newbalance.service.board.NoticeService;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final FaqServiceImpl faqService;
    private final NoticeService noticeService;

    @GetMapping("/faqs")
    public String FaqList(
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

        log.info("contents = {}, page = {}, count = {}", faqList.getContent(), page, dataCnt);

        model.addAttribute("contents", faqList.getContent());
        model.addAttribute("page", page);
        model.addAttribute("count", dataCnt <= 0 ? 0 : dataCnt);
        model.addAttribute("tag", tag);
        model.addAttribute("condition", condition);

        return "board/faqs";
    }

    @GetMapping("/api/faqs")
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

        log.info("contents = {}, page = {}, count = {}", faqList.getContent(), page, dataCnt);

        Map<String, Object> data = new HashMap<>();
        data.put("contents", faqList.getContent());
        data.put("page", page);
        data.put("count", dataCnt <= 0 ? 0 : dataCnt);
        data.put("tag", tag);
        data.put("condition", condition);

        return data;
    }


    /**
     * 공지사항 리스트
     * 더보기 페이징
     */
    @GetMapping(value = "/notice")
    public String getNotices(Model model,
                             @RequestParam(value = "offset", defaultValue = "0") int offset,
                             @RequestParam(value = "limit", defaultValue = "10") int limit) {

        List<NoticeDto> notices = noticeService.getNotice(offset, limit);
        long totalNotices = noticeService.getNoticeCount();

        model.addAttribute("notices", notices);
        model.addAttribute("offset", offset);
        model.addAttribute("limit", limit);
        model.addAttribute("totalNotices", totalNotices);

        return "board/noticeList"; // Thymeleaf template name
    }

    @GetMapping(value = "/api/notices")
    @ResponseBody
    public Map<String, Object> getNoticesJson(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                              @RequestParam(value = "limit", defaultValue = "10") int limit) {

        List<NoticeDto> notices = noticeService.getNotice(offset, limit);
        long totalNotices = noticeService.getNoticeCount();

        Map<String, Object> response = new HashMap<>();
        response.put("notices", notices);
        response.put("totalNotices", totalNotices);

        return response;
    }

    /**
     * 공지사항 상세 폼
     * 조회수  증가
     */
    @GetMapping(value = "/notice/notice-detail/{noticeId}")
    public String detailNoticeForm(@PathVariable("noticeId") Long noticeId,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Model model) {

        Notice noticeDto = noticeService.findNoticeById(noticeId);
        noticeService.updateCount(noticeId, request, response);
        model.addAttribute("noticeDto", noticeDto);

        return "board/noticeDetail";
    }

    /**
     * 공지사항 수정 폼
     */
    @GetMapping(value = "/notice/edit-form/{noticeId}")
    public String editNoticeForm(@PathVariable("noticeId") Long noticeId, Model model) {

        Notice noticeDto = noticeService.findNoticeById(noticeId);
        model.addAttribute("noticeDto", noticeDto);

        return "board/noticeEditForm";
    }

    /**
     * 공지사항 글 수정
     */
    @PostMapping(value = "/notice/edit/{noticeId}")
    public String updateNotice(@PathVariable("noticeId") Long noticeId, @ModelAttribute("noticeDto") NoticeDto noticeDto) {

        noticeService.updateNotice(noticeId, noticeDto);

        return "redirect:/notice";
    }

    /**
     * 공지사항 글 삭제
     */
    @GetMapping(value = "/notice/delete/{noticeId}")
    public String deleteNotice(@PathVariable("noticeId") Long noticeId) {

        noticeService.deleteNotice(noticeId);

        return "redirect:/notice";
    }

    /**
     * 공지사항 등록 페이지 이동
     *
     * @param model
     * @return
     */
    @GetMapping(value = "/admin/notice-form")
    public String noticeForm(Authentication authentication,
                             Model model) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        model.addAttribute("author", userDetails.getUsername());
        model.addAttribute("noticeDto", new NoticeDto());

        return "board/noticeForm";
    }

    /**
     * 공지사항 등록
     *
     * @param noticeDto
     * @return
     */
    @PostMapping(value = "/admin/add-notice")
    public String noticeAdd(NoticeDto noticeDto) {

        noticeService.saveNotice(noticeDto);

        return "redirect:/admin-page";
    }
}
