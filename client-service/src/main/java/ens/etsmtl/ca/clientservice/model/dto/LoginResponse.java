package ens.etsmtl.ca.clientservice.model.dto;

public record LoginResponse(
        Long clientId,
        String email,
        String firstName,
        String lastName,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String scope
) {
}
