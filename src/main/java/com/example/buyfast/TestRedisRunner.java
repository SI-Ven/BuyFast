package com.example.buyfast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestRedisRunner implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("⚡⚡⚡ STARTING REDIS CONNECTION TEST ⚡⚡⚡");

        try {
            // 1. Write a test value
            redisTemplate.opsForValue().set("my_test_key", "Hello Upstash!");
            System.out.println("✅ Write Successful: Saved 'my_test_key'");

            // 2. Read it back
            Object value = redisTemplate.opsForValue().get("my_test_key");
            System.out.println("✅ Read Successful: Retrieved '" + value + "'");

            if ("Hello Upstash!".equals(value)) {
                System.out.println("🎉 SUCCESS! Your Spring Boot is connected to Upstash Redis.");
            }
        } catch (Exception e) {
            System.err.println("❌ FAILED to connect to Redis. Check your password!");
            e.printStackTrace();
        }
    }
}