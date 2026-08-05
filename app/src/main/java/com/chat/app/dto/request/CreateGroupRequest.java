package com.chat.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String description;

    private List<String> memberIds;
}
