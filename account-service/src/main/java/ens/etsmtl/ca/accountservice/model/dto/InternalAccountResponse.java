package ens.etsmtl.ca.accountservice.model.dto;

import ens.etsmtl.ca.accountservice.model.AccountStatus;

public record InternalAccountResponse(
        Long accountId,
        Long clientId,
        AccountStatus status
) {
}
