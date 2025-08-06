package com.alextim.bank.front.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/front/login")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    @GetMapping
    public String logInPage(@RequestParam(required = false) List<String> errors,
                            Model model) {
        log.info("Incoming request for getting loginPage page");
        if (errors != null && !errors.isEmpty()) {
            model.addAttribute("errors", errors);
        }
        return "login";
    }
}
