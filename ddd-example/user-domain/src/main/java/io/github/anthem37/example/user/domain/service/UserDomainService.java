package io.github.anthem37.example.user.domain.service;

import io.github.anthem37.ddd.domain.service.AbstractDomainService;
import io.github.anthem37.example.user.domain.model.User;
import io.github.anthem37.example.user.domain.repository.IUserRepository;
import io.github.anthem37.example.user.domain.specification.UserSpecification;
import io.github.anthem37.example.user.domain.valueobject.Email;

/**
 * 用户领域服务
 * 展示DDD框架的领域服务特性
 */
public class UserDomainService extends AbstractDomainService {
    
    private final IUserRepository userRepository;
    
    public UserDomainService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * 检查用户是否可以注册
     * 业务规则：用户名和邮箱都不能重复
     */
    public void checkUserCanRegister(String username, Email email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已存在: " + email.getValue());
        }
    }
    
    /**
     * 检查用户是否可以更新邮箱
     * 业务规则：新邮箱不能被其他用户使用
     */
    public void checkUserCanUpdateEmail(User user, Email newEmail) {
        if (user.getEmail().equals(newEmail)) {
            return; // 邮箱没有变化
        }
        
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("邮箱已被其他用户使用: " + newEmail.getValue());
        }
    }
    
    /**
     * 验证用户密码强度
     * 业务规则：密码至少8位，包含字母和数字
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("密码长度至少8位");
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("密码必须包含字母和数字");
        }
    }
}