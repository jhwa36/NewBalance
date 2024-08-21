package practice.newbalance.repository.item.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import practice.newbalance.dto.item.CartInfoDto;
import java.util.Optional;

import static practice.newbalance.domain.item.QCart.cart;
import static practice.newbalance.domain.item.QProduct.product;
import static practice.newbalance.domain.item.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CustomCartRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<CartInfoDto> getProductInCart(Long cartId) {

        return Optional.ofNullable(queryFactory.select(
                        Projections.constructor(
                                CartInfoDto.class,
                                cart.id,
                                product.title,
                                productOption.color,
                                productOption.size,
                                cart.count,
                                cart.price
                        )
                )
                .from(cart)
                .innerJoin(product)
                .on(cart.product.id.eq(product.id))
                .innerJoin(productOption)
                .on(cart.productOption.id.eq(productOption.id))
                .where(cart.id.eq(cartId))
                .fetchOne());
    }
}
