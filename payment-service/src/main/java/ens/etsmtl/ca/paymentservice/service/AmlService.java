package ens.etsmtl.ca.paymentservice.service;

import ens.etsmtl.ca.paymentservice.repository.TransferRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class AmlService {

    private static final BigDecimal AML_AMOUNT_THRESHOLD = new BigDecimal("10000.00");
    private static final long AML_MAX_TRANSFERS_PER_HOUR = 20L;

    private final TransferRepository transferRepository;

    public AmlService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public boolean isSuspicious(Long sourceAccountId, BigDecimal amount) {
        if (amount.compareTo(AML_AMOUNT_THRESHOLD) > 0) {
            return true;
        }
        long transferCountLastHour = transferRepository.countBySourceAccountIdAndCreationDateAfter(
                sourceAccountId,
                OffsetDateTime.now().minusHours(1)
        );
        return transferCountLastHour >= AML_MAX_TRANSFERS_PER_HOUR;
    }
}
