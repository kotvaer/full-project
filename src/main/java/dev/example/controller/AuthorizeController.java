package dev.example.controller;

import dev.example.entity.Rest;
import dev.example.entity.request.EmailRegisterVO;
import dev.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService accountService;

    @GetMapping("/ask-code")
    public Rest<Void> askVerifyCode(@RequestParam @Email String email,
                                    @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                    HttpServletRequest request) {
        return messageHandler(() ->
                accountService.registerEmailVerifyCode(type, email, request.getRemoteAddr()));

    }

    @PostMapping("/register")
    public Rest<Void> register(@RequestBody @Valid EmailRegisterVO vo) {
        return messageHandler(() -> accountService.registerEmailAccount(vo));
    }

    private Rest<Void> messageHandler(Supplier<String> action) {
        String message = action.get();
        return message == null ? Rest.success() : Rest.failure(400, message);
    }
}
