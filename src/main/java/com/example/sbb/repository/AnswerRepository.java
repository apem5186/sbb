package com.example.sbb.repository;

import com.example.sbb.entity.board.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
}
