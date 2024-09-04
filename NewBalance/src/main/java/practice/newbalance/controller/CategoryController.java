package practice.newbalance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import practice.newbalance.domain.item.CategoryEnum;
import practice.newbalance.dto.item.CategoryDto;
import practice.newbalance.service.item.CategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 서브메뉴 조회
     * @param title
     * @return
     */
    @GetMapping("/categories")
    public Map<Integer, List<CategoryDto>> getCategoriesByTitle(@RequestParam("title") CategoryEnum title) {
        return categoryService.getGroupedCategoriesByTitle(title);
    }

}
