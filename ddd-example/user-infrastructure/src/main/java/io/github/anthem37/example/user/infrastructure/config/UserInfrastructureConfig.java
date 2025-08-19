package io.github.anthem37.example.user.infrastructure.config;

import io.github.anthem37.example.user.domain.repository.IUserRepository;
import io.github.anthem37.example.user.domain.service.UserDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用户基础设施配置
 * 展示DDD框架的配置特性
 */
@Configuration
public class UserInfrastructureConfig {
    
    /**
     * 配置用户领域服务
     */
    @Bean
    public UserDomainService userDomainService(IUserRepository userRepository) {
        return new UserDomainService(userRepository);
    }
}