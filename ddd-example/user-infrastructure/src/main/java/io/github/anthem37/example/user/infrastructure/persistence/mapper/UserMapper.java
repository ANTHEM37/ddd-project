package io.github.anthem37.example.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import io.github.anthem37.example.user.infrastructure.persistence.entity.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 * 展示DDD框架的MyBatis-Plus集成
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
    
    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    UserPO findByEmail(@Param("email") String email);
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    UserPO findByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * 查找活跃用户
     */
    @Select("SELECT * FROM users WHERE status = #{status}")
    List<UserPO> findByStatus(@Param("status") UserStatus status);
    
    /**
     * 分页查询用户
     */
    @Select("SELECT * FROM users ORDER BY created_at DESC LIMIT #{offset}, #{size}")
    List<UserPO> findUsersWithPagination(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计用户总数
     */
    @Select("SELECT COUNT(*) FROM users")
    long countUsers();
}