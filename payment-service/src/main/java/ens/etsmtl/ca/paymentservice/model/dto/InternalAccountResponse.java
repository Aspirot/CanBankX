package ens.etsmtl.ca.paymentservice.model.dto;

public record InternalAccountResponse(
        Long accountId,
        Long clientId,
        String status
) {
}
