package ens.etsmtl.ca.accountservice.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LedgerEntryRequest(
        Long accountId,
        Long clientId,
        String eventType,
        BigDecimal amount,
        String description,
        OffsetDateTime createdAt
) {
}
