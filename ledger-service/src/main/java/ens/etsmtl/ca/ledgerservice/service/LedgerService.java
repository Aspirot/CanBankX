package ens.etsmtl.ca.ledgerservice.service;

import ens.etsmtl.ca.ledgerservice.model.LedgerEntry;
import ens.etsmtl.ca.ledgerservice.model.EntryType;
import ens.etsmtl.ca.ledgerservice.model.dto.CreateLedgerEntryRequest;
import ens.etsmtl.ca.ledgerservice.repository.LedgerEntryRepository;
import ens.etsmtl.ca.ledgerservice.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class LedgerService {
    private static final ZoneId STATEMENT_ZONE = ZoneId.of("America/Montreal");

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void appendEntry(CreateLedgerEntryRequest request) {
        if (ledgerEntryRepository.existsByIdempotencyKey(request.idempotencyKey())) {
            return;
        }

        if (request.entryType() == EntryType.ACCOUNT_CREATION && request.transactionId() != 0L) {
            throw new ValidationException("transactionId must be 0 when entryType is ACCOUNT_CREATION");
        }
        if (request.entryType() != EntryType.ACCOUNT_CREATION && request.transactionId() <= 0L) {
            throw new ValidationException("transactionId must be greater than 0 for DEBIT/CREDIT");
        }
        if (request.entryType() == EntryType.DEBIT) {
            BigDecimal balance = getAccountBalance(request.accountId());
            if (balance.compareTo(request.amount()) < 0) {
                throw new ValidationException("Insufficient funds");
            }
        }

        LedgerEntry entry = new LedgerEntry();
        entry.setAccountId(request.accountId());
        entry.setTransactionId(request.transactionId());
        entry.setIdempotencyKey(request.idempotencyKey());
        entry.setAmount(request.amount());
        entry.setDate(request.date());
        entry.setEntryType(request.entryType());
        ledgerEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(Long accountId) {
        BigDecimal credits = ledgerEntryRepository.sumAmountByAccountIdAndEntryTypeIn(
                accountId,
                List.of(EntryType.ACCOUNT_CREATION, EntryType.CREDIT)
        );
        BigDecimal debits = ledgerEntryRepository.sumAmountByAccountIdAndEntryTypeIn(
                accountId,
                List.of(EntryType.DEBIT)
        );
        return credits.subtract(debits);
    }

    @Transactional(readOnly = true)
    public BigDecimal getDailyDifference(Long accountId, LocalDate date) {
        OffsetDateTime from = date.atStartOfDay(STATEMENT_ZONE).toOffsetDateTime();
        OffsetDateTime to = date.plusDays(1).atStartOfDay(STATEMENT_ZONE).toOffsetDateTime();

        BigDecimal totalIn = ledgerEntryRepository.sumAmountByAccountIdAndDateBetweenAndEntryTypeIn(
                accountId,
                from,
                to,
                List.of(EntryType.ACCOUNT_CREATION, EntryType.CREDIT)
        );
        BigDecimal totalOut = ledgerEntryRepository.sumAmountByAccountIdAndDateBetweenAndEntryTypeIn(
                accountId,
                from,
                to,
                List.of(EntryType.DEBIT)
        );
        return totalIn.subtract(totalOut);
    }
}
