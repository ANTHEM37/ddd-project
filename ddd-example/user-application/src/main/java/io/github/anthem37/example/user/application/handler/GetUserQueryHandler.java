package io.github.anthem37.example.user.application.handler;

import io.github.anthem37.ddd.common.cqrs.query.IQueryHandler;
import io.github.anthem37.example.user.application.dto.UserDTO;
import io.github.anthem37.example.user.application.query.GetUserQuery;
import io.github.anthem37.example.user.domain.model.User;
import io.github.anthem37.example.user.domain.repository.IUserRepository;
import io.github.anthem37.example.user.domain.valueobject.UserId;
import org.springframework.stereotype.Component;

/**
 * 获取用户查询处理器
 * 展示DDD框架的查询处理器特性
 */
@Component
public class GetUserQueryHandler implements IQueryHandler<GetUserQuery, UserDTO> {

    private final IUserRepository userRepository;

    public GetUserQueryHandler(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Class<GetUserQuery> getSupportedQueryType() {
        return GetUserQuery.class;
    }

    @Override
    public UserDTO handle(GetUserQuery query) {
        UserId userId = UserId.of(query.getUserId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + query.getUserId()));

        return new UserDTO(
                user.getId().getValue(),
                user.getUsername(),
                user.getEmail().getValue(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}