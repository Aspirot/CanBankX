package ens.etsmtl.ca.ledgerservice.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummaryResponse(
        Long accountId,
        LocalDate date,
        BigDecimal dailySummary
) {
}
