package ens.etsmtl.ca.clientservice.model.dto;

import ens.etsmtl.ca.clientservice.model.ClientStatus;

public record RegistrationResponse(
        Long clientId,
        String email,
        ClientStatus status,
        String message
) {
}
