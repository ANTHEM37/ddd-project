package io.github.anthem37.example.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.anthem37.ddd.common.converter.ConverterRegistry;
import io.github.anthem37.ddd.infrastructure.persistence.repository.AbstractBaseRepository;
import io.github.anthem37.example.user.domain.model.User;
import io.github.anthem37.example.user.domain.repository.IUserRepository;
import io.github.anthem37.example.user.domain.valueobject.Email;
import io.github.anthem37.example.user.domain.valueobject.UserId;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import io.github.anthem37.example.user.infrastructure.persistence.converter.UserConverter;
import io.github.anthem37.example.user.infrastructure.persistence.entity.UserPO;
import io.github.anthem37.example.user.infrastructure.persistence.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储实现
 * 展示DDD框架的仓储模式特性
 */
@Repository
public class UserRepositoryImpl extends AbstractBaseRepository<User, UserId> implements IUserRepository {

    @Autowired
    private UserMapper userMapper;

    @Override
    protected Optional<User> doFindById(UserId userId) {
        UserPO userPO = userMapper.selectById(userId);
        return Optional.ofNullable(ConverterRegistry.getConverter(UserConverter.class).toDomain(userPO));
    }

    @Override
    protected void doInsert(User aggregate) {
        UserPO persistence = ConverterRegistry.getConverter(UserConverter.class).toPersistence(aggregate);
        userMapper.insert(persistence);
    }

    @Override
    protected void doUpdate(User aggregate) {
        UserPO persistence = ConverterRegistry.getConverter(UserConverter.class).toPersistence(aggregate);
        userMapper.updateById(persistence);
    }

    @Override
    protected void doDeleteById(UserId userId) {
        userMapper.deleteById(userId);
    }

    @Override
    protected List<User> doFindAll() {
        return ConverterRegistry.getConverter(UserConverter.class).toDomainList(userMapper.selectList(new QueryWrapper<>()));
    }

    @Override
    protected boolean doExistsById(UserId userId) {

        return userMapper.selectCount(new QueryWrapper<UserPO>().eq("id", userId)) > 0;
    }

    @Override
    protected long doCount() {

        return userMapper.selectCount(new QueryWrapper<>());
    }

    @Override
    public Optional<User> findByEmail(Email email) {

        return Optional.ofNullable(ConverterRegistry.getConverter(UserConverter.class).toDomain(userMapper.findByEmail(email.getValue())));
    }

    @Override
    public Optional<User> findByUsername(String username) {

        return Optional.ofNullable(ConverterRegistry.getConverter(UserConverter.class).toDomain(userMapper.findByUsername(username)));
    }

    @Override
    public boolean existsByEmail(Email email) {

        return userMapper.existsByEmail(email.getValue());
    }

    @Override
    public boolean existsByUsername(String username) {

        return userMapper.existsByUsername(username);
    }

    @Override
    public List<User> findActiveUsers() {

        return ConverterRegistry.getConverter(UserConverter.class).toDomainList(userMapper.selectList(new QueryWrapper<UserPO>().eq("status", UserStatus.ACTIVE)));
    }
}