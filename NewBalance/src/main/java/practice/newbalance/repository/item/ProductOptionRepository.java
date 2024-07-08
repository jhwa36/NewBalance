package practice.newbalance.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.newbalance.domain.item.Cart;
import practice.newbalance.domain.item.ProductOption;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    public Cart findByColorAndSize(String color, String size);

    void deleteByColorAndIdIn(String color, List<Integer> optionId);
    List<ProductOption> findByColorAndIdIn(String color, List<Integer> optionId);

    Long deleteByProductId(Long productId);
}
