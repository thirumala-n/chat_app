package com.chat.app.mapper;

import com.chat.app.dto.response.AttachmentResponse;
import com.chat.app.dto.response.ReactionResponse;
import com.chat.app.dto.response.UserResponse;
import com.chat.app.entity.Attachment;
import com.chat.app.entity.MessageReaction;
import com.chat.app.entity.Role;
import com.chat.app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    UserResponse toResponse(User user);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
