package ens.etsmtl.ca.clientservice.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{9}$", message = "SIN must contain exactly 9 digits") String sin,
        @NotBlank @Pattern(regexp = "^\\+?[0-9\\- ]{7,25}$", message = "Invalid phone format") String phone,
        @NotBlank @Size(min = 12, max = 100) String password
) {
}
