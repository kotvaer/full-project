package dev.example.entity.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailRegisterVO {
    @Email
    String email;
    @Length(min = 6, max = 6)
    String code;
    //正则表达式，不允许包含特殊字符
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")
    @Length(max = 10)
    String username;
    @Length(min = 6, max = 20)
    String password;
}
