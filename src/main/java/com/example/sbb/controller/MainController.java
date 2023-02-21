package com.example.sbb.controller;

import com.example.sbb.dto.ProfileDTO;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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

    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @GetMapping("/getProfile")
    public ProfileDTO profile(Principal principal) {

        SiteUser siteUser = userService.getUser(principal.getName());
        return new ProfileDTO(siteUser);
    }
}
