package io.github.anthem37.example.user.domain.valueobject;

import io.github.anthem37.ddd.domain.model.AbstractValueObject;
import lombok.Getter;

import java.util.UUID;

/**
 * 用户ID值对象
 * 展示DDD框架的值对象特性
 */
@Getter
public class UserId extends AbstractValueObject {

    private final String value;

    private UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        this.value = value;
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{value};
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public AbstractValueObject copy() {
        return new UserId(this.value);
    }
}
