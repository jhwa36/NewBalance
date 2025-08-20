package practice.newbalance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import practice.newbalance.domain.member.Member;
import practice.newbalance.dto.member.MemberDto;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    @Query("select m.userId from Member m where m.name = :name and m.phoneNumber = :phoneNumber")
    String findInquiryIdByNameAndPhoneNumber(
            @Param("name") String name,
            @Param("phoneNumber") String phoneNumber
    );


    Member findByUserId(String userId);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    @Query("select m from Member m where m.userId = :userId and m.name = :name and m.phoneNumber = :phoneNumber")
    Optional<Member> findByUserId(
            @Param("userId") String userId,
            @Param("name") String name,
            @Param("phoneNumber") String phoneNumber
    );
}
