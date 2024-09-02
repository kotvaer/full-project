package dev.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.example.entity.dto.Account;
import dev.example.entity.request.EmailRegisterVO;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;


public interface AccountService extends IService<Account>, UserDetailsService {
    Account findByUsernameOrEmail(String text);

    String registerEmailVerifyCode(String type, String email, String ip);

    String registerEmailAccount(EmailRegisterVO vo);
}
