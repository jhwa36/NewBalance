package practice.newbalance.service.item;

import practice.newbalance.domain.item.CategoryEnum;
import practice.newbalance.dto.item.CategoryDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CategoryService {

    List<CategoryDto> findByCategory(String title);

    CategoryDto addItem(CategoryDto categoryDto);


    void editItem(Long categoryId, CategoryDto categoryDto);

    void deleteItem(Long categoryId);

    List<CategoryDto> findDetailedCategories(String parentTitle, Integer subCategoryRef);

    List<CategoryDto> getAllCategories();

    Map<Integer, List<CategoryDto>> getGroupedCategoriesByTitle(CategoryEnum title);

    List<CategoryDto> findByTitleAndRef(CategoryEnum title, Integer categoryRef);
}
