package com.portal.placementportal.dto;

import com.portal.placementportal.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record StudentRegisterRequest(
            @NotBlank(message = "usn is required")
            @Size(min = 3, max = 32, message = "usn must be between 3 and 32 characters")
            String usn,

            @NotBlank(message = "email is required")
            @Email(message = "email must be a valid email address")
            String email,

            @NotNull(message = "collegeId is required")
            @Positive(message = "collegeId must be positive")
            Long collegeId
    ) {}

    public record LoginRequest(
            @NotBlank(message = "username/usn is required")
            String username,

            @NotBlank(message = "password is required")
            String password
    ) {}

    public record LoginResponse(Long userId, String username, Role role, Long collegeId, String message) {}

    public record CreateAdminRequest(
            @NotBlank(message = "username is required")
            @Size(min = 3, max = 64)
            String username,

            @NotBlank(message = "fullName is required")
            String fullName,

            @NotBlank(message = "email is required")
            @Email
            String email,

            @NotBlank(message = "password is required")
            @Size(min = 8, message = "password must be at least 8 characters")
            String password,

            @NotNull(message = "collegeId is required")
            @Positive
            Long collegeId
    ) {}

    public record CreateSuperAdminRequest(
            @NotBlank @Size(min = 3, max = 64) String username,
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password
    ) {}

    public record RegisterResponse(Long studentId, String usn, String email, String message) {}

    public record CreateCollegeRequest(
            @NotBlank(message = "collegeName is required")
            @Size(max = 255)
            String collegeName,

            @Size(max = 500)
            String address
    ) {}

    public record RegistrationStatusUpdateRequest(
            @NotBlank(message = "status is required")
            String status
    ) {}
}
