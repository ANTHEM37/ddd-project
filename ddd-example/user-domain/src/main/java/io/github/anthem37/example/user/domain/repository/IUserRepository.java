package io.github.anthem37.example.user.domain.repository;

import io.github.anthem37.ddd.domain.repository.IRepository;
import io.github.anthem37.example.user.domain.model.User;
import io.github.anthem37.example.user.domain.valueobject.Email;
import io.github.anthem37.example.user.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 * 展示DDD框架的仓储模式
 */
public interface IUserRepository extends IRepository<User, UserId> {
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(Email email);
    
    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 查找活跃用户列表
     */
    List<User> findActiveUsers();

}