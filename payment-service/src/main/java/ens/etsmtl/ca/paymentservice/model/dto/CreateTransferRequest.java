package ens.etsmtl.ca.paymentservice.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotNull @Positive Long sourceAccountId,
        @Positive Long destinationAccountId,
        @Email String destinationEmail,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}
