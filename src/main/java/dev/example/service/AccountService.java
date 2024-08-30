package dev.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.example.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;


public interface AccountService extends IService<Account>, UserDetailsService {
    Account findByUsernameOrEmail(String text);
}
