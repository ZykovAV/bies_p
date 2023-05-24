package ylab.bies.userservice.mapper;

import org.mapstruct.Mapper;
import ylab.bies.userservice.dto.RoleResponse;
import ylab.bies.userservice.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toRoleResponse(Role role);
}
