package practice.newbalance.repository.item.query;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import practice.newbalance.dto.item.ProductDto;
import practice.newbalance.dto.item.ProductOptionDto;
import practice.newbalance.dto.item.ProductOptionDtoDetails;

import java.util.List;

import static practice.newbalance.domain.item.QProduct.product;
import static practice.newbalance.domain.item.QProductOption.productOption;


@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements CustomProductRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductDto> getProductDetail(Long productId) {

        List<ProductDto> fetch = queryFactory.select(
                Projections.constructor(
                        ProductDto.class,
                        product.id,
                        product.title,
                        product.content,
                        product.code,
                        product.contry,
                        product.material,
                        product.features,
                        product.price,
                        product.manufactureDate,
                        product.category,
                        Projections.list(
                                Projections.constructor(
                                        ProductOptionDto.class,
                                        productOption.color,
                                        Projections.list(
                                                Projections.constructor(
                                                        ProductOptionDtoDetails.class,
                                                        productOption.size,
                                                        productOption.quantity
                                                )
                                        )
                                )
                        )
                )
        ).from(product)
                .innerJoin(productOption)
                .on(product.id.eq(productOption.product.id))
                .where(product.id.eq(productId)).fetch();

        return fetch;

    }

    @Override
    public List<ProductOptionDto> getProductOption(Long productId) {
        List<ProductOptionDto> fetch = queryFactory.select(
                        Projections.constructor(
                                ProductOptionDto.class,
                                productOption.color,
                                Projections.list(
                                        Projections.constructor(
                                                ProductOptionDtoDetails.class,
                                                productOption.id,
                                                productOption.size,
                                                productOption.quantity
                                        )
                                )
                        )
                ).from(productOption)
                .where(productOption.product.id.eq(productId)).fetch();
        return fetch;
    }
}
