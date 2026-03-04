package ens.etsmtl.ca.accountservice.model.dto;

import ens.etsmtl.ca.accountservice.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull @Positive Long clientId,
        @NotNull AccountType type,
        @NotNull @DecimalMin(value = "0.00") BigDecimal initialBalance
) {
}
