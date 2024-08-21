package practice.newbalance.repository.item.query;

import practice.newbalance.dto.item.CartInfoDto;

import java.util.Optional;

public interface CustomCartRepository {
    Optional<CartInfoDto> getProductInCart(Long cartId);
}
