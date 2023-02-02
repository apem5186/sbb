package com.example.sbb.service.user;

import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.entity.user.UserRole;
import com.example.sbb.exception.DataNotFoundException;
import com.example.sbb.repository.AnswerRepository;
import com.example.sbb.repository.QuestionRepository;
import com.example.sbb.repository.UserRepository;
import com.example.sbb.service.AnswerService;
import com.example.sbb.service.QuestionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final AnswerService answerService;
    private final QuestionService questionService;

    @Transactional
    public SiteUser create(String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        this.userRepository.save(user);
        return user;
    }

    @Transactional
    public SiteUser modify(String username, String email, String password, SiteUser siteUser) {
        siteUser.modify(username, passwordEncoder.encode(password), email);
        userRepository.save(siteUser);
        return siteUser;
    }

    public SiteUser getUser(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByUsername(username);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            log.info("------------------------------");
            log.info("USERNAME : " + username);
            log.info("------------------------------");
            throw new DataNotFoundException("siteuser not found");
        }
    }
    public SiteUser getUserByEmail(String email) {
        Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            log.info("------------------------------");
            log.info("Email : " + email);
            log.info("------------------------------");
            throw new DataNotFoundException("siteuser not found");
        }
    }

    public SiteUser getUserWithId(Long id) {
        Optional<SiteUser> siteUser = this.userRepository.findById(id);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteuser not found");
        }
    }

    @Transactional
    public void delete(SiteUser siteUser) {
        List<Answer> answerList = siteUser.getAnswerList();
        List<Question> questionList = siteUser.getQuestionList();
        answerRepository.deleteVote(siteUser.getId());
        questionRepository.deleteVote(siteUser.getId());
        for (Answer answer : answerList) {
            answerService.delete(answer);
        }
        for (Question question : questionList) {
            questionService.delete(question);
        }
        this.userRepository.delete(siteUser);
    }
}
