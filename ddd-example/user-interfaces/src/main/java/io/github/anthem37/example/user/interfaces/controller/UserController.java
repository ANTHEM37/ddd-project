package io.github.anthem37.example.user.interfaces.controller;

import io.github.anthem37.ddd.common.orchestration.Orchestration;
import io.github.anthem37.ddd.interfaces.dto.BaseResponse;
import io.github.anthem37.ddd.interfaces.dto.DataResponse;
import io.github.anthem37.ddd.interfaces.facade.AbstractBaseFacade;
import io.github.anthem37.example.user.application.dto.UserDTO;
import io.github.anthem37.example.user.application.service.UserApplicationService;
import io.github.anthem37.example.user.interfaces.converter.UserInterfaceConverter;
import io.github.anthem37.example.user.interfaces.dto.CreateUserRequest;
import io.github.anthem37.example.user.interfaces.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 展示DDD框架的接口层特性
 */
@RestController
@RequestMapping("/api/users")
public class UserController extends AbstractBaseFacade {

    @Autowired
    private UserApplicationService userApplicationService;

    @Autowired
    private UserInterfaceConverter userInterfaceConverter;

    /**
     * 创建用户
     */
    @PostMapping("/create")
    public DataResponse<String> createUser(@RequestBody CreateUserRequest request) {
        try {
            String userId = userApplicationService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
            return DataResponse.success(userId, "用户创建成功");
        } catch (Exception e) {
            return DataResponse.error("CREATE_USER_ERROR", e.getMessage());
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public DataResponse<UserResponse> getUserById(@PathVariable String id) {
        try {
            UserDTO userDTO = userApplicationService.getUser(id);
            if (userDTO == null) {
                return DataResponse.error("USER_NOT_FOUND", "用户不存在");
            }
            UserResponse response = userInterfaceConverter.assemble(userDTO);
            return DataResponse.success(response);
        } catch (Exception e) {
            return DataResponse.error("GET_USER_ERROR", e.getMessage());
        }
    }

    /**
     * 激活用户
     */
    @PutMapping("/{id}/activate")
    public BaseResponse activateUser(@PathVariable String id) {
        try {
            userApplicationService.activateUser(id);
            return BaseResponse.success("用户激活成功");
        } catch (Exception e) {
            return BaseResponse.failure("ACTIVATE_USER_ERROR", e.getMessage());
        }
    }

    /**
     * 停用用户
     */
    @PutMapping("/{id}/deactivate")
    public BaseResponse deactivateUser(@PathVariable String id) {
        try {
            userApplicationService.deactivateUser(id);
            return BaseResponse.success("用户停用成功");
        } catch (Exception e) {
            return BaseResponse.failure("DEACTIVATE_USER_ERROR", e.getMessage());
        }
    }

    /**
     * 使用业务编排创建用户
     * 展示DDD框架的业务编排特性
     */
    @PostMapping("/orchestration")
    public DataResponse<Object> createUserWithOrchestration(@RequestBody CreateUserRequest request) {
        try {
            Orchestration.Result result = userApplicationService.createUserWithOrchestration(request.getUsername(), request.getEmail(), request.getPassword());

            if (result.isSuccess()) {
                return DataResponse.success(result.getResults(), "用户注册成功");
            } else {
                return DataResponse.error("ORCHESTRATION_ERROR", result.getErrorMessage());
            }
        } catch (Exception e) {
            return DataResponse.error("ORCHESTRATION_ERROR", e.getMessage());
        }
    }

    /**
     * 获取用户注册流程图
     * 展示DDD框架的业务编排可视化特性
     */
    @GetMapping("/registration-flow")
    public DataResponse<String> getRegistrationFlowDiagram() {
        try {
            String plantUML = userApplicationService.getUserRegistrationFlowDiagram();
            return DataResponse.success(plantUML, "流程图获取成功");
        } catch (Exception e) {
            return DataResponse.error("GET_FLOW_ERROR", e.getMessage());
        }
    }
}