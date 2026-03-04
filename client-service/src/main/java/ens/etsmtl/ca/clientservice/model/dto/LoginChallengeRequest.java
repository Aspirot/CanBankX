package ens.etsmtl.ca.clientservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginChallengeRequest(
        @NotBlank @Size(max = 100) String challengeId,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "OTP must contain exactly 6 digits") String otp
) {
}
