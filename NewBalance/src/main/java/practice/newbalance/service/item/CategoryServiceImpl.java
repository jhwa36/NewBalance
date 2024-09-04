package practice.newbalance.service.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.newbalance.domain.item.Category;
import practice.newbalance.domain.item.CategoryEnum;
import practice.newbalance.dto.item.CategoryDto;
import practice.newbalance.repository.item.CategoryRepository;
import practice.newbalance.repository.item.query.CustomCategoryRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private final CustomCategoryRepository customCategoryRepository;
    private final CategoryRepository categoryRepository;


    public CategoryServiceImpl(CustomCategoryRepository customCategoryRepository,CategoryRepository categoryRepository) {
        this.customCategoryRepository = customCategoryRepository;
        this.categoryRepository = categoryRepository;
    }

    //카테고리 조회
    @Override
    public List<CategoryDto> findByCategory(String title) {
        return customCategoryRepository.findByCategory(title);
    }

    //카테고리 등록
    @Override
    public CategoryDto addItem(CategoryDto categoryDto) {

//        boolean isChecked = categoryRepository.existsByTitle(String.valueOf(categoryDto.getTitle()));
//        중복체크 로직
//        if(isChecked){
//
//        }
        Category saveCategory = categoryRepository.save(categoryDto.toEntity());
        return saveCategory.toDto();
    }

    //카테고리 수정
    @Override
    @Transactional
    public void editItem(Long categoryId, CategoryDto categoryDto) {

        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

        if (optionalCategory.isPresent()) {
            Category findCategory = optionalCategory.get();


            findCategory.setName(categoryDto.getName());
            findCategory.setTitle(categoryDto.getTitle());
            findCategory.setRef(categoryDto.getRef());
            findCategory.setStep(categoryDto.getStep());

            categoryRepository.save(findCategory);
        }
    }

    //카테고리 삭제
    @Override
    public void deleteItem(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    //카테고리 상세 조회
    @Override
    public List<CategoryDto> findDetailedCategories(String parentTitle, Integer subCategoryRef) {
        return customCategoryRepository.findDetailedCategories(parentTitle, subCategoryRef);
    }

    //카테고리 전체조회
    @Override
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(Category::toDto)
                .collect(Collectors.toList());
    }

    // 카테고리 목록 조회
    @Override
    public Map<Integer, List<CategoryDto>> getGroupedCategoriesByTitle(CategoryEnum title) {
        List<Category> categories = categoryRepository.findByTitle(title);

        return categories.stream()
                .map(Category::toDto)
                .collect(Collectors.groupingBy(CategoryDto::getRef));
    }
}
