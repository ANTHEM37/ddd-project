package io.github.anthem37.example.user.interfaces.converter;

import io.github.anthem37.ddd.interfaces.assembler.AbstractDTOAssembler;
import io.github.anthem37.example.user.application.dto.UserDTO;
import io.github.anthem37.example.user.interfaces.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * 用户接口转换器
 * 展示DDD框架的接口层转换器特性
 */
@Component
public class UserInterfaceConverter extends AbstractDTOAssembler<UserResponse, UserDTO> {

    @Override
    public UserResponse assemble(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        return new UserResponse(
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getEmail(),
                userDTO.getStatus(),
                userDTO.getCreatedAt(),
                userDTO.getUpdatedAt()
        );
    }

    @Override
    public UserDTO disassemble(UserResponse userResponse) {
        if (userResponse == null) {
            return null;
        }

        return new UserDTO(
                userResponse.getId(),
                userResponse.getUsername(),
                userResponse.getEmail(),
                userResponse.getStatus(),
                userResponse.getCreatedAt(),
                userResponse.getUpdatedAt()
        );
    }
}