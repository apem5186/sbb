package com.example.sbb.service;

import com.example.sbb.dto.AnswerForm;
import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.exception.DataNotFoundException;
import com.example.sbb.repository.AnswerRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    public Page<Answer> getList(int page, Integer id) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("regDate"));
        Pageable pageable = PageRequest.of(page-1, 5, Sort.by(sorts));
        Specification<Answer> spec = specAnswer(id);
        return this.answerRepository.findAll(spec, pageable);
    }

    public Answer create(Question question, String content, SiteUser author) {
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setContent(content);
        answer.setAuthor(author);
        this.answerRepository.save(answer);
        return answer;
    }

    public Answer getAnswer(Integer id) {
        Optional<Answer> answer = this.answerRepository.findById(id);
        if (answer.isPresent()) {
            return answer.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    public void modify(Answer answer, String content) {
        answer.setContent(content);
        this.answerRepository.save(answer);
    }

    public Answer getHigherVoter(Integer id) {
        List<Answer> answerList = answerRepository.findAnswerByQuestionId(id);
        int voterSize = 0;
        Answer answers = new Answer();
        for (Answer answer : answerList) {
            if (answer.getVoter().size() > voterSize) {
                voterSize = answer.getVoter().size();
                answers = answer;
            }
        }
        if (voterSize == 0) {
            return null;
        }
        return answers;
    }

    public void delete(Answer answer) {
        this.answerRepository.delete(answer);
    }

    public void vote(Answer answer, SiteUser siteUser) {
        answer.getVoter().add(siteUser);
        this.answerRepository.save(answer);
    }

    private Specification<Answer> specAnswer(Integer id) {
        return new Specification<Answer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Answer> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("question").get("id"), id);
            }
        };
    }
}
