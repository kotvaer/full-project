package dev.example.controller;

import dev.example.entity.Rest;
import dev.example.entity.request.ConfirmResetVO;
import dev.example.entity.request.EmailRegisterVO;
import dev.example.entity.request.EmailResetPasswordVO;
import dev.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
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
        //return messageHandler(() -> accountService.registerEmailAccount());
        return messageHandler(vo, accountService::registerEmailAccount);
    }

    //此出将验证码单独拆出成为一个独立的接口，其实这个接口没必要单独出，此处是为了方便测试
    //ask-code、配合reset-password实现密码重置
    @PostMapping("/reset-confirm")
    public Rest<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo) {
        //return messageHandler(() -> accountService.resetConfirm(vo));
        return messageHandler(vo, accountService::resetConfirm);
    }

    @PostMapping("/reset-password")
    public Rest<Void> resetConfirm(@RequestBody @Valid EmailResetPasswordVO vo) {
        //return messageHandler(() -> accountService.resetConfirm(vo));
        return messageHandler(vo, accountService::resetAccountPasswordByPassword);
    }

    private <T> Rest<Void> messageHandler(T vo, Function<T, String> function) {
        return messageHandler(() -> function.apply(vo));
    }

    private Rest<Void> messageHandler(Supplier<String> action) {
        String message = action.get();
        return message == null ? Rest.success() : Rest.failure(400, message);
    }
}
