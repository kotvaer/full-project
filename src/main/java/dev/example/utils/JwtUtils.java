package dev.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    private String key;

    @Value("${spring.security.jwt.expire}")
    private int expire;

    @Resource
    StringRedisTemplate template;

    public boolean invalidateJwt(String authHeader) {
        //获取jwt令牌内容
        String token = this.convertToken(authHeader);
        if (token == null) return false;

        //选择加密算法和key生成Algorithm，用于验证签名
        Algorithm algorithm = Algorithm.HMAC256(key);

        //创建algorithm对应的JWT验证器
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            //verify方法验证token内容（没有抛出异常说明token是我们签发的），并验证token有没有过期
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getId();
            System.out.println("invalid:"+id);
            return addTokenToBlackList(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean addTokenToBlackList(String uuid, Date time) {
        //判断这个jwt是否在redis的黑名单中
        if (isInBlackList(uuid)) {
            return true;
        }
        //计算jwt的剩余有效时长
        //可能这个jwt已经过期，此时expire为负数，此时将其有效时长记为0即可
        //此处有一个坑，set过期时长为0会报错，必须>=0
        Date now = new Date();
        long expire = Math.max(time.getTime()- now.getTime(), 0);
        //将jwt存入redis的黑名单中
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MICROSECONDS);
        return true;
    }

    //从redis中查询黑名单是否有jwt的uuid
    public boolean isInBlackList(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

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
            DecodedJWT jwt = verifier.verify(token);
            //过滤存在黑名单中的jwt
            if (isInBlackList(jwt.getId())) {
                System.out.println("resolve:"+jwt.getId());
                return null;
            }
            Date expires = jwt.getExpiresAt();
            return new Date().after(expires) ? null : jwt;
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

    public Integer toId(DecodedJWT token) {
        return token.getClaim("id").asInt();
    }


    public String generateToken(UserDetails userDetails, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
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
