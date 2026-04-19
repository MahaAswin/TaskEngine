package com.taskengine.backend.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter public class AuthRequest { @Email @NotBlank private String email; @NotBlank private String password; }
