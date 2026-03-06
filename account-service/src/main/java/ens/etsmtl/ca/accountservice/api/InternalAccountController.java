package ens.etsmtl.ca.accountservice.api;

import ens.etsmtl.ca.accountservice.model.Account;
import ens.etsmtl.ca.accountservice.model.dto.InternalAccountResponse;
import ens.etsmtl.ca.accountservice.repository.AccountRepository;
import ens.etsmtl.ca.accountservice.service.exception.ValidationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/v1/accounts")
public class InternalAccountController {

    private final AccountRepository accountRepository;

    public InternalAccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{accountId}")
    public InternalAccountResponse getAccount(@PathVariable Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ValidationException("Account not found"));
        return new InternalAccountResponse(
                account.getId(),
                account.getClientId(),
                account.getStatus()
        );
    }
}
