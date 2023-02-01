package com.example.sbb.repository;

import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.user.SiteUser;
import org.hibernate.annotations.NamedNativeQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    Page<Answer> findAll(Specification<Answer> spec, Pageable pageable);

    List<Answer> findAnswerByQuestionId(Integer id);

    @Modifying
    @Query(value = "DELETE FROM answer_voter WHERE VOTER_ID = :id", nativeQuery = true)
    void deleteVote(@Param("id") Integer id);

}
