package ens.etsmtl.ca.ledgerservice.repository;

import ens.etsmtl.ca.ledgerservice.model.LedgerEntry;
import ens.etsmtl.ca.ledgerservice.model.EntryType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.time.OffsetDateTime;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("""
            select coalesce(sum(le.amount), 0)
            from LedgerEntry le
            where le.accountId = :accountId and le.entryType in :types
            """)
    BigDecimal sumAmountByAccountIdAndEntryTypeIn(
            @Param("accountId") Long accountId,
            @Param("types") Collection<EntryType> types
    );

    @Query("""
            select coalesce(sum(le.amount), 0)
            from LedgerEntry le
            where le.accountId = :accountId
              and le.date >= :from and le.date < :to
              and le.entryType in :types
            """)
    BigDecimal sumAmountByAccountIdAndDateBetweenAndEntryTypeIn(
            @Param("accountId") Long accountId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("types") Collection<EntryType> types
    );
}
