package com.example.sbb.service;

import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Category;
import com.example.sbb.entity.board.Question;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.exception.DataNotFoundException;
import com.example.sbb.repository.QuestionRepository;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    public Page<Question> getList(int page, String kw, String category) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("regDate"));
        Pageable pageable = PageRequest.of(page-1, 10, Sort.by(sorts));
        Specification<Question> spec = search(kw);
        spec = spec.and(category(category));
//        List<Question> questions = this.questionRepository.findAll(spec, pageable)
//                .filter(q -> q.getCategory().toString().equals(category)).stream().toList();
//        int start = (int) pageable.getOffset();
//        int end = Math.min((start + pageable.getPageSize()), questions.size());
//        Page<Question> questionList = new PageImpl<>(questions.subList(start, end), pageable, questions.size());
        return this.questionRepository.findAll(spec, pageable);
    }

    public Question getQuestion(Integer id) {
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public void create(String subject, String content, String category, SiteUser user) {
        Category category1 = Category.FREE;
        if (category.equals("question")) {
            category1 = Category.QUESTION;
        }
        Question q = new Question();
        q.setContent(content);
        q.setSubject(subject);
        q.setCategory(category1);
        q.setAuthor(user);
        this.questionRepository.save(q);
    }

    public void modify(Question question, String subject, String content, String category) {
        Category category1 = Category.FREE;
        if (category.equals("question")) {
            category1 = Category.QUESTION;
        }
        question.setContent(content);
        question.setSubject(subject);
        question.setCategory(category1);
        this.questionRepository.save(question);
    }

    public void delete(Question question) {
        this.questionRepository.delete(question);
    }

    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }

    private Specification<Question> category(String category) {
        return new Specification<Question>() {
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(q.get("category"), Category.valueOf(category));
            }
        };
    }

    private Specification<Question> search(String kw) {
        return new Specification<Question>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);   // 중복제거
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);

                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
                        cb.like(q.get("content"), "%" + kw + "%"),   // 내용
                        cb.like(u1.get("username"), "%" + kw + "%"), // 질문 작성자
                        cb.like(a.get("content"), "%" + kw + "%"),   // 답변 내용
                        cb.like(u2.get("username"), "%" + kw + "%")); // 답변 작성자
            }
        };
    }

    @Transactional
    public void updateHits(Integer id) {
        this.questionRepository.updateHits(id);
    }
}
