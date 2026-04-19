package com.taskengine.backend.dto;

import com.taskengine.backend.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteRequest {

  @NotBlank @Email private String email;

  @NotNull private UserRole role;
}
