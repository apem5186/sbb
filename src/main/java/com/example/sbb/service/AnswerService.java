package com.example.sbb.service;

import com.example.sbb.dto.AnswerForm;
import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import com.example.sbb.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    public void create(Question question, String content) {
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setContent(content);
        this.answerRepository.save(answer);
    }
}
