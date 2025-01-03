package practice.newbalance.repository.item.query;


import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;
import practice.newbalance.domain.item.QCategory;
import practice.newbalance.domain.item.QProduct;
import practice.newbalance.domain.item.QProductOption;
import practice.newbalance.domain.item.QThumbnail;
import practice.newbalance.dto.item.ProductDto;
import practice.newbalance.dto.item.ProductOptionDto;
import practice.newbalance.dto.item.ProductOptionDtoDetails;

import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

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

//    @Override
//    public Page<ProductDto> findProductsByCategoryId(Long categoryId, Pageable pageable) {
//        QProduct product = QProduct.product;
//        QProductOption productOption = QProductOption.productOption;
//        QThumbnail thumbnail = QThumbnail.thumbnail; // 썸네일을 추가로 조회하기 위한 Q객체
//
//        // 1. 전체 카운트 계산
//        Long totalResult = queryFactory
//                .select(product.count())
//                .from(product)
//                .where(product.category.id.eq(categoryId))
//                .fetchOne();
//
//        long total = (totalResult != null) ? totalResult : 0L;
//
//        // 2. Product와 ProductOption을 함께 조회
//        List<Tuple> productOptionResults = queryFactory
//                .select(
//                        product.id,
//                        product.title,
//                        product.content,
//                        product.code,
//                        product.contry,
//                        product.material,
//                        product.features,
//                        product.price,
//                        product.manufactureDate,
//                        product.category,
//                        productOption.color,
//                        productOption.size,
//                        productOption.quantity
//                )
//                .from(product)
//                .leftJoin(product.productOptions, productOption)
//                .where(product.category.id.eq(categoryId))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        // 3. 썸네일 조회
//        Map<Long, List<String>> thumbnailsMap = queryFactory
//                .select(thumbnail.product.id, thumbnail.thumbnailUrl)
//                .from(thumbnail)
//                .where(thumbnail.product.id.in(
//                        productOptionResults.stream()
//                                .map(tuple -> tuple.get(product.id))
//                                .distinct()
//                                .collect(Collectors.toList())
//                ))
//                .fetch()
//                .stream()
//                .collect(Collectors.groupingBy(
//                        tuple -> tuple.get(thumbnail.product.id),
//                        Collectors.mapping(tuple -> tuple.get(thumbnail.thumbnailUrl), Collectors.toList())
//                ));
//
//        // 4. ProductDto에 옵션 추가
//        Map<Long, ProductDto> productDtoMap = new HashMap<>();
//        for (Tuple tuple : productOptionResults) {
//            Long productId = tuple.get(product.id);
//
//            // ProductDto 객체 생성 및 옵션 추가
//            ProductDto productDto = productDtoMap.computeIfAbsent(productId, productIdKey -> new ProductDto(
//                    productIdKey,
//                    tuple.get(product.title),
//                    tuple.get(product.content),
//                    tuple.get(product.code),
//                    tuple.get(product.contry),
//                    tuple.get(product.material),
//                    tuple.get(product.features),
//                    tuple.get(product.price),
//                    tuple.get(product.manufactureDate),
//                    tuple.get(product.category),
//                    new ArrayList<>(), // 옵션 리스트 초기화
//                    thumbnailsMap.getOrDefault(productIdKey, new ArrayList<>()) // 썸네일 리스트 초기화
//            ));
//
//            // 옵션 추가
//            ProductOptionDto option = new ProductOptionDto(
//                    tuple.get(productOption.color),
//                    Collections.singletonList(
//                            new ProductOptionDtoDetails(
//                                    tuple.get(productOption.size),
//                                    tuple.get(productOption.quantity)
//                            )
//                    )
//            );
//
//            productDto.getProductOptions().add(option);
//        }
//
//        List<ProductDto> content = new ArrayList<>(productDtoMap.values());
//
//        return new PageImpl<>(content, pageable, total);
//    }

//    @Override
//    public Page<ProductDto> findProductsByCategoryId(Long categoryId, Pageable pageable) {
//        QProduct product = QProduct.product;
//        QProductOption productOption = QProductOption.productOption;
//        QThumbnail thumbnail = QThumbnail.thumbnail; // 썸네일을 추가로 조회하기 위한 Q객체
//
//        // 1. 전체 카운트 계산
//        Long totalResult = queryFactory
//                .select(product.count())
//                .from(product)
//                .where(product.category.id.eq(categoryId))
//                .fetchOne();
//
//        long total = (totalResult != null) ? totalResult : 0L;
//
//        // 2. Product와 ProductOption을 함께 조회
//        List<Tuple> productOptionResults = queryFactory
//                .select(
//                        product.id,
//                        product.title,
//                        product.content,
//                        product.code,
//                        product.contry,
//                        product.material,
//                        product.features,
//                        product.price,
//                        product.manufactureDate,
//                        product.category,
//                        productOption.color,
//                        productOption.size,
//                        productOption.quantity
//                )
//                .from(product)
//                .leftJoin(product.productOptions, productOption)
//                .where(product.category.id.eq(categoryId))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        // 3. 썸네일 조회
//        Map<Long, List<String>> thumbnailsMap = queryFactory
//                .select(thumbnail.product.id, thumbnail.thumbnailUrl)
//                .from(thumbnail)
//                .where(thumbnail.product.id.in(
//                        productOptionResults.stream()
//                                .map(tuple -> tuple.get(product.id))
//                                .distinct()
//                                .collect(Collectors.toList())
//                ))
//                .fetch()
//                .stream()
//                .collect(Collectors.groupingBy(
//                        tuple -> tuple.get(thumbnail.product.id),
//                        Collectors.mapping(tuple -> tuple.get(thumbnail.thumbnailUrl), Collectors.toList())
//                ));
//
//        // 4. ProductDto에 옵션 추가
//        Map<Long, ProductDto> productDtoMap = new HashMap<>();
//        for (Tuple tuple : productOptionResults) {
//            Long productId = tuple.get(product.id);
//
//            // ProductDto 객체 생성 및 옵션 추가
//            ProductDto productDto = productDtoMap.computeIfAbsent(productId, productIdKey -> new ProductDto(
//                    productIdKey,
//                    tuple.get(product.title),
//                    tuple.get(product.content),
//                    tuple.get(product.code),
//                    tuple.get(product.contry),
//                    tuple.get(product.material),
//                    tuple.get(product.features),
//                    tuple.get(product.price),
//                    tuple.get(product.manufactureDate),
//                    tuple.get(product.category),
//                    new ArrayList<>(), // 옵션 리스트 초기화
//                    thumbnailsMap.getOrDefault(productIdKey, new ArrayList<>()) // 썸네일 리스트 초기화
//            ));
//
//            // 옵션 추가: 여러 사이즈를 처리할 수 있도록 리스트에 추가
//            ProductOptionDtoDetails optionDetail = new ProductOptionDtoDetails(
//                    tuple.get(productOption.size),
//                    tuple.get(productOption.quantity)
//            );
//
//            // 기존의 색상 옵션을 가져와서 추가
//            ProductOptionDto option = productDto.getProductOptions().stream()
//                    .filter(o -> o.getColor().equals(tuple.get(productOption.color)))
//                    .findFirst()
//                    .orElseGet(() -> {
//                        ProductOptionDto newOption = new ProductOptionDto(
//                                tuple.get(productOption.color),
//                                new ArrayList<>() // 새로운 리스트 생성
//                        );
//                        productDto.getProductOptions().add(newOption);
//                        return newOption;
//                    });
//
//            // 옵션 세부정보 추가
//            option.getProductOptionDtoDetailsList().add(optionDetail);
//        }
//
//        List<ProductDto> content = new ArrayList<>(productDtoMap.values());
//
//        return new PageImpl<>(content, pageable, total);
//    }


    @Override
    public Page<ProductDto> findProductsByCategoryId(Long categoryId, List<String> sizes, List<String> colors, Integer minPrice, Integer maxPrice, Pageable pageable) {
        QProduct product = QProduct.product;
        QProductOption productOption = QProductOption.productOption;
        QThumbnail thumbnail = QThumbnail.thumbnail;

        // 1.전체 카운트 계산
        Long totalResult = queryFactory
                .select(product.count())
                .from(product)
                .join(product.productOptions, productOption)
                .where(product.category.id.eq(categoryId)
                        .and(sizes != null && !sizes.isEmpty() ? productOption.size.in(sizes) : null) // 사이즈 필터 (null 처리)
                        .and(colors != null && !colors.isEmpty() ? productOption.color.in(colors) : null) // 색상 필터 (null 처리)
                        .and(minPrice != null && maxPrice != null ? product.price.between(minPrice, maxPrice) : null) // 가격 필터 (null 처리)
            )
                .fetchOne();
        long total = (totalResult != null) ? totalResult : 0L;

        // 2. Prodyut와 ProductOption을 조회
        List<Tuple> productOptionResult = queryFactory
                .select(
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
                        productOption.color,
                        productOption.size,
                        productOption.quantity
                )
                .from(product)
                .leftJoin(product.productOptions, productOption)
                .where(product.category.id.eq(categoryId)
                        .and(sizes != null && !sizes.isEmpty() ? productOption.size.in(sizes) : null) // 사이즈 필터 (null 처리)
                        .and(colors != null && !colors.isEmpty() ? productOption.color.in(colors) : null) // 색상 필터 (null 처리)
                        .and(minPrice != null && maxPrice != null ? product.price.between(minPrice, maxPrice) : null) // 가격 필터 (null 처리)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3. 썸네일 조회
        Map<Long, List<String>> thumbnailsMap = queryFactory
                .select(thumbnail.product.id, thumbnail.thumbnailUrl)
                .from(thumbnail)
                .where(thumbnail.product.id.in(
                        productOptionResult.stream()
                                .map(tuple -> tuple.get(product.id))
                                .distinct()
                                .collect(Collectors.toList())
                ))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(thumbnail.product.id),
                        Collectors.mapping(tuple -> tuple.get(thumbnail.thumbnailUrl), Collectors.toList() )
                ));

        // 4. productDto에 옵션 추가
        Map<Long, ProductDto> productDtoMap = new HashMap<>();
        for(Tuple tuple : productOptionResult) {
            Long productId = tuple.get(product.id);

            ProductDto productDto = productDtoMap.computeIfAbsent(productId, productIdKey -> new ProductDto(
                    productIdKey,
                    tuple.get(product.title),
                    tuple.get(product.content),
                    tuple.get(product.code),
                    tuple.get(product.contry),
                    tuple.get(product.material),
                    tuple.get(product.features),
                    tuple.get(product.price),
                    tuple.get(product.manufactureDate),
                    tuple.get(product.category),
                    new ArrayList<>(),
                    thumbnailsMap.getOrDefault(productIdKey, new ArrayList<>())
            ));

            // 옵션 추가: 여러 사이즈를 처리할 수 있도록 리스트에 추가
            ProductOptionDtoDetails optionDetail = new ProductOptionDtoDetails(
                    tuple.get(productOption.size),
                    tuple.get(productOption.quantity)
            );

            // 기존의 색상 옵션을 가져와서 추가
            ProductOptionDto option = productDto.getProductOptions().stream()
                    .filter(o -> o.getColor().equals(tuple.get(productOption.color)))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductOptionDto newOption = new ProductOptionDto(
                                tuple.get(productOption.color), new ArrayList<>()); // 새로운 리스트 생성
                        productDto.getProductOptions().add(newOption);
                        return newOption;
                    });

            // 옵션 세부정보 추가
            option.getProductOptionDtoDetailsList().add(optionDetail);
        }

        List<ProductDto> content = new ArrayList<>(productDtoMap.values());

        return new PageImpl<>(content, pageable, total);
    }


    //검색 키워드로 상품 조회
    @Override
    public Page<ProductDto> findProductByKeyword(
            String keyword, List<String> sizes, List<String> color, Integer minPrice, Integer maxPrice, Pageable pageable) {
        QProduct product = QProduct.product;
        QProductOption option = QProductOption.productOption;
        QThumbnail thumbnail = QThumbnail.thumbnail;
        QCategory category = QCategory.category;
        // 전체 카운트 계산
        Long totalResult = queryFactory
                .select(product.count())
                .from(product)
                .join(category).on(category.id.eq(product.category.id))
                .where(
                        category.name.like(keyword).or(product.title.like(keyword))
                        .and(sizes != null && !sizes.isEmpty() ? productOption.size.in(sizes) : null )  //사이즈 필터
                        .and(color != null && !color.isEmpty() ? productOption.color.in(color) : null)  //색상 필터
                        .and(
                                minPrice != null && maxPrice != null ?
                                        product.price.between(minPrice, maxPrice) : null
                        ))  //가격 필터
                .fetchOne();

        long total = (totalResult == null)? 0L : totalResult;


        // 2. Prodyut와 ProductOption을 조회
        List<Tuple> productOptionResult = queryFactory
                .select(
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
                        productOption.color,
                        productOption.size,
                        productOption.quantity
                )
                .from(product)
                .leftJoin(product.productOptions, productOption)
                .where(product.title.like(keyword).or(category.name.like(keyword))
                        .and(sizes != null && !sizes.isEmpty() ? productOption.size.in(sizes) : null) // 사이즈 필터 (null 처리)
                        .and(color != null && !color.isEmpty() ? productOption.color.in(color) : null) // 색상 필터 (null 처리)
                        .and(minPrice != null && maxPrice != null ? product.price.between(minPrice, maxPrice) : null) // 가격 필터 (null 처리)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 썸네일 조회
        Map<Long, List<String>> thumbnailsMap = queryFactory
                .select(thumbnail.product.id, thumbnail.thumbnailUrl)
                .from(thumbnail)
                .where(thumbnail.product.id.in(
                        productOptionResult.stream()
                                .map(tuple -> tuple.get(product.id))
                                .distinct()
                                .collect(Collectors.toList())
                ))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(thumbnail.product.id),
                        Collectors.mapping(tuple -> tuple.get(thumbnail.thumbnailUrl), Collectors.toList() )
                ));

        // dto에 옵션 추가
        Map<Long, ProductDto> productDtoMap = new HashMap<>();
        for(Tuple tuple : productOptionResult) {
            Long productId = tuple.get(product.id);

            ProductDto productDto = productDtoMap.computeIfAbsent(productId, productIdKey -> new ProductDto(
                    productIdKey,
                    tuple.get(product.title),
                    tuple.get(product.content),
                    tuple.get(product.code),
                    tuple.get(product.contry),
                    tuple.get(product.material),
                    tuple.get(product.features),
                    tuple.get(product.price),
                    tuple.get(product.manufactureDate),
                    tuple.get(product.category),
                    new ArrayList<>(),
                    thumbnailsMap.getOrDefault(productIdKey, new ArrayList<>())
            ));

            // 옵션 추가: 여러 사이즈를 처리할 수 있도록 리스트에 추가
            ProductOptionDtoDetails optionDetail = new ProductOptionDtoDetails(
                    tuple.get(productOption.size),
                    tuple.get(productOption.quantity)
            );

            // 기존의 색상 옵션을 가져와서 추가
            ProductOptionDto optionDto = productDto.getProductOptions().stream()
                    .filter(o -> o.getColor().equals(tuple.get(productOption.color)))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductOptionDto newOption = new ProductOptionDto(
                                tuple.get(productOption.color), new ArrayList<>()); // 새로운 리스트 생성
                        productDto.getProductOptions().add(newOption);
                        return newOption;
                    });

            // 옵션 세부정보 추가
            optionDto.getProductOptionDtoDetailsList().add(optionDetail);
        }

        List<ProductDto> content = new ArrayList<>(productDtoMap.values());

        // 옵션 추가: 여러 사이즈를 처리할 수 있도록 리스트에 추가
        // 옵션 세부정보 추가
        return new PageImpl<>(content, pageable, total);
    }
}
