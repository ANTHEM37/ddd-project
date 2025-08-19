package io.github.anthem37.example.user.infrastructure.persistence.converter;

import io.github.anthem37.ddd.infrastructure.converter.AbstractPersistenceConverter;
import io.github.anthem37.example.user.domain.model.User;
import io.github.anthem37.example.user.domain.valueobject.Email;
import io.github.anthem37.example.user.domain.valueobject.UserId;
import io.github.anthem37.example.user.infrastructure.persistence.entity.UserPO;
import org.springframework.stereotype.Component;

/**
 * 用户领域对象与持久化对象转换器
 * 展示DDD框架的转换器特性
 */
@Component
public class UserConverter extends AbstractPersistenceConverter<User, UserPO> {

    @Override
    protected UserPO doToPersistence(User domain) {
        if (domain == null) {
            return null;
        }

        UserPO po = new UserPO();
        po.setId(domain.getId().getValue());
        po.setUsername(domain.getUsername());
        po.setEmail(domain.getEmail().getValue());
        po.setPassword(domain.getPassword());
        po.setStatus(domain.getStatus());
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());

        return po;
    }

    @Override
    protected User doToDomain(UserPO persistence) {
        if (persistence == null) {
            return null;
        }

        return new User(UserId.of(persistence.getId()), persistence.getUsername(), Email.of(persistence.getEmail()), persistence.getPassword(), persistence.getStatus(), persistence.getCreatedAt(), persistence.getUpdatedAt());
    }

}