package ens.etsmtl.ca.paymentservice.model.dto;

import ens.etsmtl.ca.paymentservice.model.TransactionStatus;

import java.math.BigDecimal;

public record TransferResponse(
        Long transactionId,
        TransactionStatus status,
        Long sourceAccountId,
        Long destinationAccountId,
        String destinationEmail,
        BigDecimal amount
) {
}
