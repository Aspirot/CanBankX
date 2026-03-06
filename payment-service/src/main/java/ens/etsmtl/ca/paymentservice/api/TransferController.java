package ens.etsmtl.ca.paymentservice.api;

import ens.etsmtl.ca.paymentservice.model.dto.CreateTransferRequest;
import ens.etsmtl.ca.paymentservice.model.dto.TransferResponse;
import ens.etsmtl.ca.paymentservice.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public TransferResponse createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return transferService.createTransfer(request, idempotencyKey);
    }
}
