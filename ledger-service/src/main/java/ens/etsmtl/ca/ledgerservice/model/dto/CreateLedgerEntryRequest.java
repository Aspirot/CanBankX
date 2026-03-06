package ens.etsmtl.ca.ledgerservice.model.dto;

import ens.etsmtl.ca.ledgerservice.model.EntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateLedgerEntryRequest(
        @NotNull Long accountId,
        @NotNull Long transactionId,
        @NotBlank String idempotencyKey,
        @NotNull @DecimalMin(value = "0.00") BigDecimal amount,
        @NotNull OffsetDateTime date,
        @NotNull EntryType entryType
) {
}
