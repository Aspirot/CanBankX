package ens.etsmtl.ca.ledgerservice.api;

import ens.etsmtl.ca.ledgerservice.model.dto.CreateLedgerEntryRequest;
import ens.etsmtl.ca.ledgerservice.model.dto.AccountBalanceResponse;
import ens.etsmtl.ca.ledgerservice.model.dto.DailySummaryResponse;
import ens.etsmtl.ca.ledgerservice.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public void appendEntry(@Valid @RequestBody CreateLedgerEntryRequest request) {
        ledgerService.appendEntry(request);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public AccountBalanceResponse getBalance(@PathVariable Long accountId) {
        return new AccountBalanceResponse(accountId, ledgerService.getAccountBalance(accountId));
    }

    @GetMapping("/accounts/{accountId}/statements/daily")
    public DailySummaryResponse getDailyStatement(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return new DailySummaryResponse(accountId, date, ledgerService.getDailyDifference(accountId, date));
    }
}
