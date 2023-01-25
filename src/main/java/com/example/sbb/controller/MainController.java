package com.example.sbb.controller;

import com.example.sbb.dto.ProfileDTO;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping("/sbb")
    @ResponseBody
    public String index() {
        return "안녕하세요 sbb에 오신것을 환영합니다!";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/question/list";
    }

    /**
     * TODO : 이 방법말고 로그인 했는지 확인하는 자바스크립트랑 컨트롤러 하나 짜서
     * TODO : 로그인 하면 profile 변수 하나 만들어서 프론트단에서 전역변수 만드는게 날듯
     */
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @GetMapping("/getProfile")
    public ProfileDTO profile(Model model, Principal principal) {
        if (principal.getName().isEmpty()) {
            log.info("===========================================");
            log.info("nullllllllllllllllllllllll");
            log.info("===========================================");
        }
        SiteUser siteUser = userService.getUser(principal.getName());
        ProfileDTO profileDTO = new ProfileDTO(siteUser);
        model.addAttribute("profileDTO", profileDTO);
        return profileDTO;
    }
}
