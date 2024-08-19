package dev.example.entity.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthorizedVO {
    String username;
    String role;
    String token;
    LocalDateTime expire;
}
