package com.example.sbb.controller.question;

import com.example.sbb.dto.AnswerForm;
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

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;



    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "category", defaultValue = "free") String category) {
        Page<Question> paging = this.questionService.getList(page, kw, category.toUpperCase());
        model.addAttribute("category", category);
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
     * https://velog.io/@juwonlee920/Spring-?????????-??????-??????-?????????-??????-??????
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
            String email = String.valueOf(oAuth2User.getAttributes().get("email"));
            siteUser = this.userService.getUserByEmail(email);
        }

        this.questionService.create(questionForm.getSubject(), questionForm.getContent(),
                questionForm.getCategory(), siteUser);
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id,
                                 Principal principal,
                                 Authentication authentication) {
        Question question = this.questionService.getQuestion(id);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            if (!question.getAuthor().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String email = String.valueOf(oAuth2User.getAttributes().get("email"));
            SiteUser siteUser = userService.getUserByEmail(email);
            if (!question.getAuthor().getUsername().equals(siteUser.getUsername())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
            }
        }

        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        questionForm.setCategory(String.valueOf(question.getCategory()));
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String email = String.valueOf(oAuth2User.getAttributes().get("email"));
            SiteUser siteUser = userService.getUserByEmail(email);
            if (!question.getAuthor().getUsername().equals(siteUser.getUsername())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
            }
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent(),
                questionForm.getCategory());
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String email = String.valueOf(oAuth2User.getAttributes().get("email"));
            SiteUser siteUser = userService.getUserByEmail(email);
            if (!question.getAuthor().getUsername().equals(siteUser.getUsername())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "??????????????? ????????????.");
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
            String email = String.valueOf(oAuth2User.getAttributes().get("email"));
            siteUser = this.userService.getUserByEmail(email);
        }
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }

}
