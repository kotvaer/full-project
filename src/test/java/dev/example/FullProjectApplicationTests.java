package dev.example;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class FullProjectApplicationTests {

    @Resource
    StringRedisTemplate template;

    @Test
    void contextLoads() {
        template.opsForValue().set("key", "val");
        System.out.println(template.opsForValue().get("key"));
    }

}
