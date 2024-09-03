package dev.example.entity.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmailResetPasswordVO extends ConfirmResetVO{
    @Length(min = 6, max = 20)
    String password;
}
