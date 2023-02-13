package com.example.sbb.repository;

import com.example.sbb.entity.board.Category;
import com.example.sbb.entity.board.Question;
import com.example.sbb.entity.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Question findBySubject(String subject);
    Question findBySubjectAndContent(String subject, String content);
    List<Question> findBySubjectLike(String subject);
    Page<Question> findAll(Pageable pageable);
    Page<Question> findAll(Specification<Question> spec, Pageable pageable);

    @Modifying
    @Query("update Question q set q.hits = q.hits + 1 where q.id =:id")
    int updateHits(@Param("id") Integer id);

    @Modifying
    @Query(value = "DELETE FROM question_voter WHERE VOTER_ID = :id", nativeQuery = true)
    void deleteVote(@Param("id") Integer id);
}
