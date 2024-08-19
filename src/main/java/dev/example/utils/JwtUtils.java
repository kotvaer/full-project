package dev.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    private String key;

    @Value("${spring.security.jwt.expire}")
    private int expire;


    //从Authorization头中解析jwt
    public DecodedJWT resolveJwt(String authHeader) {
        //获取jwt令牌内容
        String token = this.convertToken(authHeader);
        if (token == null) return null;

        //选择加密算法和key生成Algorithm，用于验证签名
        Algorithm algorithm = Algorithm.HMAC256(key);

        //创建algorithm对应的JWT验证器
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            //verify方法验证token内容（没有抛出异常说明token是我们签发的），并验证token有没有过期
            //如果一切正常，返回解码后的DecodedJWT对象
            DecodedJWT verify = verifier.verify(token);
            Date expires = verify.getExpiresAt();
            return new Date().after(expires) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    //从HTTP请求的Authorization头中获取jwt
    private String convertToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    public UserDetails toUser(DecodedJWT token) {
        Map<String, Claim> claims = token.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }


    public String generateToken(UserDetails userDetails, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", userDetails
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .withExpiresAt(expireDate())
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    public LocalDateTime expireLDT() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(expire);
        return future;
    }


    public Instant expireDate() {
        return expireLDT().atZone(ZoneId.systemDefault()).toInstant();
    }
}
