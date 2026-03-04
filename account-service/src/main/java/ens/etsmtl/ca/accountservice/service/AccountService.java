package ens.etsmtl.ca.accountservice.service;

import ens.etsmtl.ca.accountservice.model.Account;
import ens.etsmtl.ca.accountservice.model.AccountStatus;
import ens.etsmtl.ca.accountservice.model.dto.AccountResponse;
import ens.etsmtl.ca.accountservice.model.dto.AccountSummaryResponse;
import ens.etsmtl.ca.accountservice.model.dto.CreateAccountRequest;
import ens.etsmtl.ca.accountservice.model.dto.LedgerEntryRequest;
import ens.etsmtl.ca.accountservice.repository.AccountRepository;
import ens.etsmtl.ca.accountservice.service.exception.ConflictException;
import ens.etsmtl.ca.accountservice.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerClient ledgerClient;

    public AccountService(
            AccountRepository accountRepository,
            LedgerClient ledgerClient
    ) {
        this.accountRepository = accountRepository;
        this.ledgerClient = ledgerClient;
    }

    @Transactional
    public AccountResponse openAccount(CreateAccountRequest request) {
        if (request.initialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Initial balance must be greater than or equal to zero");
        }

        Account account = new Account();
        account.setClientId(request.clientId());
        account.setType(request.type());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.initialBalance());
        account.setCreatedAt(OffsetDateTime.now());
        account.setAccountNumber(generateAccountNumber());

        Account saved = accountRepository.save(account);

        ledgerClient.appendEntry(new LedgerEntryRequest(
                saved.getId(),
                saved.getClientId(),
                "ACCOUNT_OPENED",
                saved.getBalance(),
                "Account opened with initial balance",
                OffsetDateTime.now()
        ));

        return new AccountResponse(
                saved.getId(),
                saved.getClientId(),
                saved.getAccountNumber(),
                saved.getType(),
                saved.getStatus(),
                saved.getBalance(),
                saved.getCreatedAt(),
                "Account created and journalized in ledger-service"
        );
    }

    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getAccountsByClientId(Long clientId) {
        return accountRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(account -> new AccountSummaryResponse(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getType(),
                        account.getStatus(),
                        account.getBalance(),
                        account.getCreatedAt()
                ))
                .toList();
    }

    private String generateAccountNumber() {
        for (int i = 0; i < 10; i++) {
            String candidate = "CBX-" + randomDigits(10);
            if (!accountRepository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new ConflictException("Unable to generate a unique account number");
    }

    private static String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }
}
