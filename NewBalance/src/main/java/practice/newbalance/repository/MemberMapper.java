package practice.newbalance.repository;

import org.apache.ibatis.annotations.Mapper;
import practice.newbalance.dto.member.MemberDto;

import java.util.List;

@Mapper
public interface MemberMapper {
    List<MemberDto> findAll();
    MemberDto findByEmail(String email);
    void insert(MemberDto member);
    void update(MemberDto member);
    void delete(Long id);
}
