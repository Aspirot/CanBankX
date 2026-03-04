package ens.etsmtl.ca.clientservice.repository;

import ens.etsmtl.ca.clientservice.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseOrSin(String email, String sin);
}

