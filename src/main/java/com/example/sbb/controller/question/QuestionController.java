package com.example.sbb.controller.question;

import com.example.sbb.dto.AnswerForm;
import com.example.sbb.dto.ProfileDTO;
import com.example.sbb.dto.QuestionForm;
import com.example.sbb.entity.board.Answer;
import com.example.sbb.entity.board.Question;
import com.example.sbb.entity.user.SiteUser;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

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
        log.info("99999999999999999999999999999999999999");
        log.info("PRINCIPAL : " + authentication.getPrincipal());
        log.info("PRINCIPAL : " + authentication.getName());
        log.info("PRINCIPAL : " + authentication.getDetails());
        log.info("PRINCIPAL : " + authentication.getAuthorities());
        log.info("PRINCIPAL : " + authentication.getCredentials());
        log.info("99999999999999999999999999999999999999");
        // TODO : User user = (User) authentication.getPrincipal();
        // TODO : user.getUsername() 하면 닉네임 가져올 수 있지만
        // TODO : java.lang.ClassCastException: class org.springframework.security.oauth2.core.user.DefaultOAuth2User cannot be cast to class org.springframework.security.core.userdetails.User
        // TODO : cast 에러 뜨면서 안됨 구글 쳐서 해결법 강구해야함
        SiteUser siteUser = this.userService.getUser(user.getUsername());
        log.info("================================");
        log.info("SITE USER : " + siteUser.getUsername());
        log.info("================================");
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser);
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }

}
