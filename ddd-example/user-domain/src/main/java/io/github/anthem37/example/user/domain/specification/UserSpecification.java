package io.github.anthem37.example.user.domain.specification;

import io.github.anthem37.ddd.domain.specification.ISpecification;
import io.github.anthem37.example.user.domain.model.User;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;

/**
 * 用户规约
 * 展示DDD框架的规约模式特性
 */
public class UserSpecification {

    /**
     * 活跃用户规约
     */
    public static ISpecification<User> isActive() {
        return user -> user.getStatus() == UserStatus.ACTIVE;
    }

    /**
     * 邮箱匹配规约
     */
    public static ISpecification<User> hasEmail(String email) {
        return user -> user.getEmail().getValue().equals(email);
    }

    /**
     * 用户名匹配规约
     */
    public static ISpecification<User> hasUsername(String username) {
        return user -> user.getUsername().equals(username);
    }

    /**
     * 可以登录的用户规约（活跃且有邮箱）
     */
    public static ISpecification<User> canLogin() {
        return isActive().and(user -> user.getEmail() != null);
    }

    /**
     * 可以更新邮箱的用户规约（活跃用户）
     */
    public static ISpecification<User> canUpdateEmail() {
        return isActive();
    }
}