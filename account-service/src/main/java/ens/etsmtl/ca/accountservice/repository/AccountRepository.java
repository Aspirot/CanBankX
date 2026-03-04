package ens.etsmtl.ca.accountservice.repository;

import ens.etsmtl.ca.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountNumber(String accountNumber);
    List<Account> findByClientIdOrderByCreatedAtDesc(Long clientId);
}
