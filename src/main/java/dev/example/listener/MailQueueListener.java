package dev.example.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {
    @Resource
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String username;

    @RabbitHandler
    public void sendMassage(Map<String, Object> data) {
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch (type) {
            case "register" -> createSimpleMailMessage("欢迎注册网站",
                    "您的邮箱注册码为：" + code + "有效时长为三分钟，为了保障您的安全，请勿向他人泄露验证码",
                    email);
            case "reset" -> createSimpleMailMessage("密码重置邮件",
                    "你好，你正在进行密码重置操作，验证码：" + code + "，有效时间3分钟，如非本人操作，请无视",
                    email);
            default -> null;
        };
        if(message == null) return;
        mailSender.send(message);
    }

    private SimpleMailMessage createSimpleMailMessage(String subject, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }

}
