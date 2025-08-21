package io.github.anthem37.example.user.domain.valueobject;

/**
 * 用户状态枚举
 * 展示DDD框架中的枚举值对象
 */
public enum UserStatus {
    ACTIVE("激活"),
    INACTIVE("停用"),
    LOCKED("锁定"),
    DELETED("已删除");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canBeActivated() {
        return this == INACTIVE || this == LOCKED;
    }

    public boolean canBeDeactivated() {
        return this == ACTIVE;
    }
}