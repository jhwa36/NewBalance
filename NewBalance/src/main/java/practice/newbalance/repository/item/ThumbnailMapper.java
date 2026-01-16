package practice.newbalance.repository.item;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import practice.newbalance.dto.item.ThumbnailRowDto;

import java.util.List;

@Mapper
public interface ThumbnailMapper {

        List<ThumbnailRowDto> findByProductIds(
                @Param("productIds") List<Long> productIds
        );
}
