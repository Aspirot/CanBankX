package ens.etsmtl.ca.ledgerservice.model.dto;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        Long accountId,
        BigDecimal balance
) {
}
