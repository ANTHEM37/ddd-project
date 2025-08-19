package io.github.anthem37.example.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 用户管理应用启动类
 * 展示DDD框架的完整应用启动
 */
@SpringBootApplication(scanBasePackages = {
        "io.github.anthem37.example.user",
        "io.github.anthem37.ddd"
})
@MapperScan("io.github.anthem37.example.user.infrastructure.persistence.mapper")
@EnableTransactionManagement
public class UserApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}