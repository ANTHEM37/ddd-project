package io.github.anthem37.example.user;

import io.github.anthem37.example.user.application.service.UserApplicationService;
import io.github.anthem37.example.user.application.dto.UserDTO;
import io.github.anthem37.example.user.domain.valueobject.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户应用测试
 * 展示DDD框架的集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserApplicationTest {
    
    @Autowired
    private UserApplicationService userApplicationService;
    
    @Test
    public void testCreateUser() {
        // 测试创建用户
        String userId = userApplicationService.createUser(
                "testuser", 
                "test@example.com", 
                "password123"
        );
        
        assertNotNull(userId);
        
        // 验证用户创建成功
        UserDTO user = userApplicationService.getUser(userId);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }
    
    @Test
    public void testUserStatusChange() {
        // 创建用户
        String userId = userApplicationService.createUser(
                "testuser2", 
                "test2@example.com", 
                "password123"
        );
        
        // 测试停用用户
        userApplicationService.deactivateUser(userId);
        UserDTO user = userApplicationService.getUser(userId);
        assertEquals(UserStatus.INACTIVE, user.getStatus());
        
        // 测试激活用户
        userApplicationService.activateUser(userId);
        user = userApplicationService.getUser(userId);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }
    
    @Test
    public void testCreateUserWithInvalidData() {
        // 测试无效邮箱
        assertThrows(IllegalArgumentException.class, () -> {
            userApplicationService.createUser("testuser", "invalid-email", "password123");
        });
        
        // 测试弱密码
        assertThrows(IllegalArgumentException.class, () -> {
            userApplicationService.createUser("testuser", "test@example.com", "123");
        });
    }
}