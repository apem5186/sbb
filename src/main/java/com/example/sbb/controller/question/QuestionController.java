package com.example.sbb.controller.question;

import com.example.sbb.dto.AnswerForm;
import com.example.sbb.dto.QuestionForm;
import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.entity.user.UserRole;
import com.example.sbb.repository.QuestionRepository;
import com.example.sbb.service.AnswerService;
import com.example.sbb.service.QuestionService;
import com.example.sbb.service.user.UserService;
import jakarta.servlet.http.Cookie;
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
@RequestMapping("/question")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;



    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Question> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "question_list";
    }

    /**
     *
     * @param model
     * @param id
     * @param answerForm
     * @param page
     * @param request
     * @param response
     * @return
     *
     * https://velog.io/@juwonlee920/Spring-조회수-기능-구현-조회수-중복-방지
     */
    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm,
                         @RequestParam(value = "page", defaultValue = "1") int page,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        Question question = this.questionService.getQuestion(id);

        Cookie oldCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("postHits")) {
                    oldCookie = cookie;
                }
            }
        }

        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("[" + id.toString() + "]")) {
                this.questionService.updateHits(id);
                oldCookie.setValue(oldCookie.getValue() + "_[" + id + "]");
                oldCookie.setPath("/");
                oldCookie.setMaxAge(60 * 60 * 24);
                response.addCookie(oldCookie);
            }
        } else {
            this.questionService.updateHits(id);
            Cookie newCookie = new Cookie("postHits", "[" + id + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(newCookie);
        }

        Page<Answer> paging = this.answerService.getList(page, id);
        model.addAttribute("paging", paging);
        model.addAttribute("question", question);
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal,
                                 Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
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

        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser);
        return "redirect:/question/list";
    }
    // TODO : 수정도 Authentication 객체 넣어서 고치기 user랑 social이랑 구분해서
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id,
                                 Principal principal,
                                 Authentication authentication) {
        Question question = this.questionService.getQuestion(id);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            if (!question.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            if (!question.getAuthor().getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }
        }

        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id,
                                 Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            if (!question.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            if (!question.getAuthor().getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
            }
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id,
                                 Authentication authentication) {
        Question question = this.questionService.getQuestion(id);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            if (!question.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            if (!question.getAuthor().getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
            }
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id,
                               Authentication authentication) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = new SiteUser();
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            siteUser = this.userService.getUser(principal.getName());
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            siteUser = this.userService.getUser(username);
        }
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }

}
