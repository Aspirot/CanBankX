package ens.etsmtl.ca.clientservice.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationConfirmationRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 12, max = 100) String password,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "OTP must contain exactly 6 digits") String otp
) {
}
