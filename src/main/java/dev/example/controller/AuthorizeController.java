package dev.example.controller;

import dev.example.entity.Rest;
import dev.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService accountService;
    @GetMapping("/ask-code")
    public Rest<Void> askVerifyCode(@RequestParam String email,
                                    @RequestParam String type,
                                    HttpServletRequest request) {
        String message = accountService.registerEmailVerifyCode(type,email,request.getRemoteAddr());
        return message == null ? Rest.success() : Rest.failure(400, message);
    }
}
