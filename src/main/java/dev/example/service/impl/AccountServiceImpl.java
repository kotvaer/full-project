package dev.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.example.entity.dto.Account;
import dev.example.mapper.AccountMapper;
import dev.example.service.AccountService;
import dev.example.utils.Const;
import dev.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Resource
    FlowUtils utils;

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findByUsernameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException(username);
        }
        return User.withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()){
            if(!this.verifyLimit(ip)){
                return "请求频繁请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(900000) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail",data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA+email,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public Account findByUsernameOrEmail(String text) {
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }

    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key, 60);
    }
}
