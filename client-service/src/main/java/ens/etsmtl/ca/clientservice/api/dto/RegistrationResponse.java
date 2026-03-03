package ens.etsmtl.ca.clientservice.api.dto;

import ens.etsmtl.ca.clientservice.domain.ClientStatus;

public record RegistrationResponse(
        Long clientId,
        String email,
        ClientStatus status,
        String message
) {
}
