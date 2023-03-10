package com.example.sbb.controller.user;

import com.example.sbb.dto.MailDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

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
                    "2?????? ??????????????? ???????????? ????????????.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(),
                    userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "?????? ????????? ??????????????????.");
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
            String email = (String) oAuth2User.getAttributes().get("email");
            siteUser = userService.getUserByEmail(email);
//            if (siteUser.getProvider().equals("google")) {
//                String username = String.valueOf(oAuth2User.getAttributes().get("name"));
//                siteUser = this.userService.getUser(username);
//            } else if (siteUser.getProvider().equals("naver")) {
//                String username = String.valueOf(oAuth2User.getAttributes().get("nickname"));
//                siteUser = this.userService.getUser(username);
//            }

        }
        ProfileDTO profileDTO = new ProfileDTO(siteUser);
        model.addAttribute("profileDTO", profileDTO);
        return "user_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify")
    public String modify(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, Principal principal,
                         Model model, Authentication authentication) {
        SiteUser siteUser = new SiteUser();
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if (authority.toString().equals("[ROLE_USER]")) {
            siteUser = this.userService.getUser(principal.getName());
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String email = (String) oAuth2User.getAttributes().get("email");
            siteUser = userService.getUserByEmail(email);
//            if (siteUser.getProvider().equals("google")) {
//                String username = String.valueOf(oAuth2User.getAttributes().get("name"));
//                siteUser = this.userService.getUser(username);
//            } else if (siteUser.getProvider().equals("naver")) {
//                String username = String.valueOf(oAuth2User.getAttributes().get("nickname"));
//                siteUser = this.userService.getUser(username);
//            }

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
                    "2?????? ??????????????? ???????????? ????????????.");
            model.addAttribute("profileDTO", profileDTO);
            return "user_detail";
        }
        try {
            userService.modify(userCreateForm.getUsername(), userCreateForm.getEmail(),
                    userCreateForm.getPassword1(), siteUser);
            /* ????????? ?????? ?????? */
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

    @GetMapping("/find/username")
    public String findUsername() {
        return "username_find_form";
    }

    @PostMapping("/find/username")
    public String findUsername(String email, Model model) {
        Optional<SiteUser> siteUser = userRepository.findByEmail(email);
        if (siteUser.isPresent()) {
            model.addAttribute("username", siteUser.get().getUsername());
        } else {
            model.addAttribute("username", "none");
        }
        return "username_find_form";
    }

    @GetMapping("/find/password")
    public String findPassword() {
        return "password_find_form";
    }

    @PostMapping("/find/password")
    public String findPassword(String username, String email, Model model) {
        if (userRepository.findByUsernameAndEmail(username, email).isEmpty()) {
            model.addAttribute("noMatchUsernameAndEmail",
                    "????????? ?????? ???????????? ???????????? ???????????? ????????????.");
        } else {
            MailDTO dto = userService.createMailAndChangePassword(email);
            userService.mailSend(dto);
            model.addAttribute("emailSendMsg",
                    email + "??? ???????????? ??????????????????. ????????????????????? ????????? ??? ??????????????? ????????? ?????????.");
        }

        return "password_find_form";
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "?????? ????????? ????????????.");
            }
        } else if (authority.toString().equals("[ROLE_SOCIAL]")) {

            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            String email = String.valueOf(oAuth2User.getAttributes().get("email"));
            SiteUser siteUser1 = userService.getUserByEmail(email);
            if (!siteUser.getUsername().equals(siteUser1.getUsername())) {
                log.info("========================================");
                log.info("SITEUSERNAME : " + siteUser.getUsername());
                log.info("OAUTH2NAME : " + siteUser1.getUsername());
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
