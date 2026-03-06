package ens.etsmtl.ca.accountservice.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LedgerEntryRequest(
        Long accountId,
        Long transactionId,
        String idempotencyKey,
        BigDecimal amount,
        OffsetDateTime date,
        String entryType
) {
}
