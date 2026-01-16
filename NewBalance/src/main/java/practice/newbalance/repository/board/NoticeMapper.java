package practice.newbalance.repository.board;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import practice.newbalance.dto.board.NoticeDto;

import java.util.List;

@Mapper
public interface NoticeMapper {

    List<NoticeDto> findAll(@Param("limit") int limit, @Param("offset") int offset);

    NoticeDto findById(Long id);

    long findCount();
    void insertNotice(NoticeDto notice);
    int updateNotice(NoticeDto noticeDto);
    void deleteNotice(Long id);


}
