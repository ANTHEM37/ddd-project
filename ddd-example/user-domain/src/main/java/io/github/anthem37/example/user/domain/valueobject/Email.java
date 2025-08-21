package io.github.anthem37.example.user.domain.valueobject;

import io.github.anthem37.ddd.domain.model.AbstractValueObject;

import java.util.regex.Pattern;

/**
 * 邮箱值对象
 * 展示DDD框架的值对象特性和业务规则验证
 */
public class Email extends AbstractValueObject {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private final String value;

    private Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        this.value = value.toLowerCase();
    }

    public static Email of(String value) {
        return new Email(value);
    }

    public String getValue() {
        return value;
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
        return new Email(this.value);
    }
}
