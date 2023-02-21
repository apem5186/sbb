package com.example.sbb.service.user;

import com.example.sbb.dto.MailDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;
    private static String FROM_ADDRESS;

    @Value("${spring.mail.username}")
    public void setFromAddress(String FROM_ADDRESS) {
        UserService.FROM_ADDRESS = FROM_ADDRESS;
    }
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

    public MailDTO createMailAndChangePassword(String email) {
        String str = getTempPassword();
        MailDTO dto = new MailDTO();
        dto.setAddress(email);
        dto.setTitle("SBB 임시비밀번호 안내 이메일 입니다.");
        dto.setMessage("안녕하세요. SBB 임시비밀번호 안내 관련 이메일 입니다." + " 회원님의 임시 비밀번호는 "
                + str + " 입니다." + "로그인 후에 비밀번호를 변경을 해주세요");
        updatePassword(str, email);
        return dto;
    }

    public String getTempPassword(){
        char[] charSet = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

        StringBuilder str = new StringBuilder();

        int idx = 0;
        for (int i = 0; i < 10; i++) {
            idx = (int) (charSet.length * Math.random());
            str.append(charSet[idx]);
        }
        return str.toString();
    }

    public void updatePassword(String str,String email){
        String pw = passwordEncoder.encode(str);
        int id = userRepository.findByEmail(email).orElseThrow().getId();
        userRepository.updateUserPassword(id,pw);
    }

    public void mailSend(MailDTO mailDTO) {
        System.out.println("전송 완료!");
        log.info(FROM_ADDRESS);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailDTO.getAddress());
        message.setSubject(mailDTO.getTitle());
        message.setText(mailDTO.getMessage());
        message.setFrom(FROM_ADDRESS);
        message.setReplyTo(FROM_ADDRESS);
        System.out.println("message"+message);
        mailSender.send(message);
    }
}
