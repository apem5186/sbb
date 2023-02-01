package com.example.sbb.controller.answer;

import com.example.sbb.dto.AnswerForm;
import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.service.AnswerService;
import com.example.sbb.service.QuestionService;
import com.example.sbb.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collection;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/answer")
public class AnswerController {

    private final AnswerService answerService;
    private final QuestionService questionService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createAnswer(Model model, @PathVariable("id") Integer id,
                               @Valid AnswerForm answerForm, BindingResult bindingResult,
                               Principal principal,
                               Authentication authentication) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = new SiteUser();
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        log.info("----------" + authority);
        if (authority.toString().equals("[ROLE_USER]")) {
            siteUser = this.userService.getUser(principal.getName());
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            siteUser = this.userService.getUser(username);
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question);
            return "question_detail";
        }

        Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
        return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(AnswerForm answerForm, @PathVariable("id") Integer id,
                               Principal principal,
                               Authentication authentication) {
        Answer answer = this.answerService.getAnswer(id);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            if (!answer.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            if (!answer.getAuthor().getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }
        }
        answerForm.setContent(answer.getContent());
        return "answer_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(@Valid AnswerForm answerForm,
                               BindingResult bindingResult, @PathVariable("id") Integer id,
                               Principal principal,
                               Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "answer_form";
        }
        Answer answer = this.answerService.getAnswer(id);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            if (!answer.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            if (!answer.getAuthor().getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }
        }
        this.answerService.modify(answer, answerForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String answerDelete(Principal principal, @PathVariable("id") Integer id,
                               Authentication authentication) {
        Answer answer = this.answerService.getAnswer(id);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            if (!answer.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            if (!answer.getAuthor().getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
            }
        }
        this.answerService.delete(answer);
        return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String answerVote(Principal principal, @PathVariable("id") Integer id,
                             Authentication authentication) {
        Answer answer = this.answerService.getAnswer(id);
        SiteUser siteUser = new SiteUser();
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            siteUser = this.userService.getUser(principal.getName());
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            siteUser = this.userService.getUser(username);
        }
        this.answerService.vote(answer, siteUser);
        return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
    }

    @GetMapping("/getHigherVote/{id}")
    public void getHigherVote(Model model, @PathVariable("id") Integer id) {
        Answer answer = this.answerService.getHigherVoter(id);
        model.addAttribute("HighVotedAnswer", answer);
    }
}
