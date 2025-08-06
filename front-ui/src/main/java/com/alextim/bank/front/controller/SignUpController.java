package com.alextim.bank.front.controller;

import com.alextim.bank.common.client.AccountServiceClient;
import com.alextim.bank.common.dto.account.AccountFullResponse;
import com.alextim.bank.common.dto.account.AccountRequest;
import com.alextim.bank.front.dto.SignUpForm;
import com.alextim.bank.front.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.alextim.bank.common.client.util.AccountClientUtils.createAccount;
import static com.alextim.bank.front.util.Utils.encode;

@Controller
@RequestMapping("/front/signup")
@RequiredArgsConstructor
@Slf4j
public class SignUpController {

    private final AccountServiceClient accountServiceClient;

    @GetMapping
    public String signUpPage(@RequestParam(required = false) List<String> errors,
                             Model model) {
        log.info("Incoming request for getting SingUp page");
        if (errors != null && !errors.isEmpty()) {
            model.addAttribute("errors", errors);
        }

        return "signup";
    }

    @PostMapping
    public String signUp(@Valid SignUpForm form,
                         BindingResult bindingResult) {
        log.info("Incoming request for SingUp");

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .peek(s -> log.info("Validation error: {}", s))
                    .map(Utils::encode)
                    .toList();

            return "redirect:/signup?errors=" + String.join(",", errors);
        }

        if (!form.password().equals(form.confirmPassword())) {
            return "redirect:/signup?errors=" + String.join(",",  List.of(encode("Пароли не совпадают")));
        }

        try {
            String[] name = form.name().split(" ");

            AccountRequest request = AccountRequest.builder()
                    .login(form.login())
                    .password(form.password())
                    .firstName(name[0].trim())
                    .lastName(name[1].trim())
                    .birthDate(form.birthdate())
                    .email(form.email())
                    .telegram(form.telegram())
                    .build();

            log.info("Request for account service for creating account");
            AccountFullResponse account = createAccount(accountServiceClient, request);
            log.info("Response from account service: {}", account);

            return "redirect:/login";

        } catch (RuntimeException e) {
            log.error("Error creating account", e);
            return "redirect:/signup?errors=" + String.join(",",  List.of(encode("Ошибка при создании аккаунта")));
        }
    }


}
