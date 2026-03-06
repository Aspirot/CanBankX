package ens.etsmtl.ca.paymentservice.service;

import ens.etsmtl.ca.paymentservice.model.TransactionStatus;
import ens.etsmtl.ca.paymentservice.model.Transfer;
import ens.etsmtl.ca.paymentservice.model.dto.CreateTransferRequest;
import ens.etsmtl.ca.paymentservice.model.dto.InternalAccountResponse;
import ens.etsmtl.ca.paymentservice.model.dto.LedgerEntryRequest;
import ens.etsmtl.ca.paymentservice.model.dto.TransferResponse;
import ens.etsmtl.ca.paymentservice.repository.TransferRepository;
import ens.etsmtl.ca.paymentservice.service.clients.AccountClient;
import ens.etsmtl.ca.paymentservice.service.clients.LedgerClient;
import ens.etsmtl.ca.paymentservice.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountClient accountClient;
    private final LedgerClient ledgerClient;
    private final AmlService amlService;
    private final NotificationService notificationService;

    public TransferService(
            TransferRepository transferRepository,
            AccountClient accountClient,
            LedgerClient ledgerClient,
            AmlService amlService,
            NotificationService notificationService
    ) {
        this.transferRepository = transferRepository;
        this.accountClient = accountClient;
        this.ledgerClient = ledgerClient;
        this.amlService = amlService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ValidationException("Missing Idempotency-Key header");
        }
        Transfer existing = transferRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }
        if (request.destinationAccountId() == null && (request.destinationEmail() == null || request.destinationEmail().isBlank())) {
            throw new ValidationException("destinationAccountId or destinationEmail is required");
        }

        InternalAccountResponse sourceAccount = accountClient.getAccount(request.sourceAccountId());
        ensureActive(sourceAccount, "Source account must be ACTIVE");

        Transfer transfer = new Transfer();
        transfer.setIdempotencyKey(idempotencyKey);
        transfer.setSourceAccountId(request.sourceAccountId());
        transfer.setDestinationAccountId(request.destinationAccountId());
        transfer.setDestinationEmail(request.destinationEmail());
        transfer.setAmount(request.amount());
        transfer.setCreationDate(OffsetDateTime.now());
        transfer.setStatus(TransactionStatus.PENDING);

        if (amlService.isSuspicious(request.sourceAccountId(), request.amount())) {
            transfer.setStatus(TransactionStatus.REJECTED);
            return toResponse(transferRepository.save(transfer));
        }

        Transfer saved = transferRepository.save(transfer);

        ledgerClient.appendEntry(new LedgerEntryRequest(
                saved.getSourceAccountId(),
                saved.getId(),
                idempotencyKey + "-debit",
                saved.getAmount(),
                OffsetDateTime.now(),
                "DEBIT"
        ));

        if (saved.getDestinationAccountId() != null) {
            InternalAccountResponse destinationAccount = accountClient.getAccount(saved.getDestinationAccountId());
            ensureActive(destinationAccount, "Destination account must be ACTIVE");

            if (!sourceAccount.clientId().equals(destinationAccount.clientId())) {
                throw new ValidationException("For different clients, transfer must be done by destinationEmail");
            }

            saved.setDestinationClientId(destinationAccount.clientId());
            ledgerClient.appendEntry(new LedgerEntryRequest(
                    saved.getDestinationAccountId(),
                    saved.getId(),
                    idempotencyKey + "-credit",
                    saved.getAmount(),
                    OffsetDateTime.now(),
                    "CREDIT"
            ));

            saved.setStatus(TransactionStatus.COMPLETED);
        } else {
            notificationService.sendTransferInvitation(
                    saved.getDestinationEmail(),
                    saved.getId(),
                    saved.getAmount()
            );
        }

        return toResponse(transferRepository.save(saved));
    }

    private void ensureActive(InternalAccountResponse account, String message) {
        if (!"ACTIVE".equals(account.status())) {
            throw new ValidationException(message);
        }
    }

    private TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getStatus(),
                transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getDestinationEmail(),
                transfer.getAmount()
        );
    }
}
