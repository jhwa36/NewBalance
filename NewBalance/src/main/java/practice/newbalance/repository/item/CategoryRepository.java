package practice.newbalance.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.newbalance.domain.item.Category;
import practice.newbalance.domain.item.CategoryEnum;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByTitle(String title);

    List<Category> findByTitle(CategoryEnum title);

    List<Category> findByTitleAndRef(CategoryEnum title, Integer categoryRef);

}
