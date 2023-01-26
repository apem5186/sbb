package com.example.sbb;

import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.repository.AnswerRepository;
import com.example.sbb.repository.QuestionRepository;
import com.example.sbb.repository.UserRepository;
import com.example.sbb.service.AnswerService;
import com.example.sbb.service.QuestionService;
import com.example.sbb.service.user.UserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class SbbApplicationTests {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testJpa() {
        for (int i = 1; i <= 300; i++) {
            String subject = String.format("테스트 데이터입니다:[%03d]", i);
            String content = "내용무";
            this.questionService.create(subject, content, null);
        }

    }

    @Test
    void testAnswer() {
        SiteUser user02 = userService.create("user02", "user02@test.com", "2222");
        Question question = questionService.getQuestion(309);
        for (int i = 1; i <= 100; i++) {
            this.answerService.create(question, "테스트 데이터_" + i, user02);
        }
    }

    @Test
    void modifyUser() {
        SiteUser user = userRepository.findByUsername("").orElseThrow();
        SiteUser user01 = userService.modify("user01", "user01@test.com", "1111", user);
        System.out.println("=================================");
        System.out.println("USER01 : " + user01);
        System.out.println("=================================");
    }

    @Test
    void answerTest() {
        Page<Answer> answer = answerService.getList(1, 300);
        System.out.println("========================================");
        System.out.println(answer.get().toList());
        System.out.println("========================================");
    }
    @Test
    void contextLoads() {
    }


}
