package com.ddd.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * DDD框架自动配置类
 * 提供框架核心组件的自动装配
 *
 * @author anthem37
 * @date 2025/8/14 13:47:56
 */
@Slf4j
@Configuration
@EnableAsync
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.ddd.application", "com.ddd.domain", "com.ddd.infrastructure"})
public class DddFrameworkAutoConfiguration {

}
