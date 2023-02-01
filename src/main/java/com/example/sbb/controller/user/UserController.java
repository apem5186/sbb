package com.example.sbb.controller.user;

import com.example.sbb.dto.ProfileDTO;
import com.example.sbb.dto.UserCreateForm;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.repository.UserRepository;
import com.example.sbb.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.util.Collection;

@RequiredArgsConstructor
@Controller
@Slf4j
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(),
                    userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify")
    public String modify(UserCreateForm userCreateForm, Model model, Principal principal,
                         Authentication authentication) {
        SiteUser siteUser = new SiteUser();
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            siteUser = this.userService.getUser(principal.getName());
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            siteUser = this.userService.getUser(username);
        }
        ProfileDTO profileDTO = new ProfileDTO(siteUser);
        model.addAttribute("profileDTO", profileDTO);
        return "user_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify")
    public String modify(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, Principal principal,
                         Model model, Authentication authentication) {
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        SiteUser siteUser = new SiteUser();
        if (authority.toString().equals("[ROLE_USER]")) {
            siteUser = this.userService.getUser(principal.getName());
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            siteUser = this.userService.getUser(username);
        }
        ProfileDTO profileDTO = new ProfileDTO(siteUser);
        if (bindingResult.hasErrors()) {
            log.info("SITE USER : " + siteUser.getEmail());
            log.info("PROFILEDTO : " + profileDTO.getEmail());
            model.addAttribute("profileDTO", profileDTO);
            return "user_detail";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            model.addAttribute("profileDTO", profileDTO);
            return "user_detail";
        }
        try {
            userService.modify(userCreateForm.getUsername(), userCreateForm.getEmail(),
                    userCreateForm.getPassword1(), siteUser);
            /* 변경된 세션 등록 */
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userCreateForm.getUsername(), userCreateForm.getPassword1()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("modifyFailed", e.getMessage());
        }

        return "redirect:/user/modify";
    }

    @PostMapping("/duplCheck")
    @ResponseBody
    public String duplCheck(Principal principal, String username,
                            String email) {
        SiteUser siteUser = userService.getUser(principal.getName());
        if (!email.equals(siteUser.getEmail())) {
            if (userRepository.findByEmail(email).isPresent()) {
                return "emailError";
            }
        }
        if (!username.equals(siteUser.getUsername())) {
            if (userRepository.findByUsername(username).isPresent()) {
                return "usernameError";
            }
        }
        return "ok";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String delete(Principal principal, @PathVariable("id") Long id,
                         HttpServletRequest request,
                         Authentication authentication) {
        SiteUser siteUser = this.userService.getUserWithId(id);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            if (!siteUser.getUsername().equals(principal.getName())) {
                log.info("========================================");
                log.info("SITEUSERNAME : " + siteUser.getUsername());
                log.info("PRINCIPALNAME : " + principal.getName());
                log.info("========================================");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String username = String.valueOf(oAuth2User.getAttributes().get("name"));
            if (!siteUser.getUsername().equals(username)) {
                log.info("========================================");
                log.info("SITEUSERNAME : " + siteUser.getUsername());
                log.info("OAUTH2NAME : " + username);
                log.info("========================================");
            }
        }

        log.info("SITEEEEEE : " + siteUser.getId());
        log.info("SITEEEEEE : " + siteUser.getAnswerList());
        log.info("SITEEEEEE : " + siteUser.getQuestionList());

        this.userService.delete(siteUser);
        HttpSession session = request.getSession(false);
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/question/list";
    }
}
