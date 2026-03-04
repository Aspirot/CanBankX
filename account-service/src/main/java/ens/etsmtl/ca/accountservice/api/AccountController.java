package ens.etsmtl.ca.accountservice.api;

import ens.etsmtl.ca.accountservice.model.dto.AccountResponse;
import ens.etsmtl.ca.accountservice.model.dto.AccountSummaryResponse;
import ens.etsmtl.ca.accountservice.model.dto.CreateAccountRequest;
import ens.etsmtl.ca.accountservice.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.openAccount(request);
    }

    @GetMapping
    public List<AccountSummaryResponse> getAccountsByClientId(
            @RequestParam @Positive Long clientId
    ) {
        return accountService.getAccountsByClientId(clientId);
    }
}
