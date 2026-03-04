package ens.etsmtl.ca.accountservice.model.dto;

import ens.etsmtl.ca.accountservice.model.AccountStatus;
import ens.etsmtl.ca.accountservice.model.AccountType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AccountResponse(
        Long accountId,
        Long clientId,
        String accountNumber,
        AccountType type,
        AccountStatus status,
        BigDecimal balance,
        OffsetDateTime createdAt,
        String message
) {
}
