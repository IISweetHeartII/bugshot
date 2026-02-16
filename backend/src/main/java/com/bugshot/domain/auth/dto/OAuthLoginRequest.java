package com.bugshot.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthLoginRequest {

    @NotBlank(message = "Provider is required")
    private String provider; // github, google

    @NotBlank(message = "Provider ID is required")
    private String providerId;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String name;

    private String image;
}
