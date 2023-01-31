package com.example.sbb.repository;

import com.example.sbb.entity.board.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    Page<Answer> findAll(Specification<Answer> spec, Pageable pageable);

    List<Answer> findAnswerByQuestionId(Integer id);


}
