package practice.newbalance.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.newbalance.domain.item.Category;
import practice.newbalance.domain.item.CategoryEnum;
import practice.newbalance.dto.item.CategoryDto;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByTitle(String title);

    List<Category> findByTitle(CategoryEnum title);

}
