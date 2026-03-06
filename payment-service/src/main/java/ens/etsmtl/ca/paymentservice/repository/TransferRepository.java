package ens.etsmtl.ca.paymentservice.repository;

import ens.etsmtl.ca.paymentservice.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
    long countBySourceAccountIdAndCreationDateAfter(Long sourceAccountId, OffsetDateTime threshold);
}
