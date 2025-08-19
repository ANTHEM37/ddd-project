package io.github.anthem37.example.user.application.query;

import io.github.anthem37.ddd.application.query.IQuery;
import io.github.anthem37.example.user.application.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 获取用户查询
 * 展示DDD框架的查询模式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GetUserQuery implements IQuery<UserDTO> {

    private String userId;
}