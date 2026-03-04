package ens.etsmtl.ca.clientservice.service;

import ens.etsmtl.ca.clientservice.model.Client;
import ens.etsmtl.ca.clientservice.model.ClientStatus;
import ens.etsmtl.ca.clientservice.model.dto.RegistrationConfirmationRequest;
import ens.etsmtl.ca.clientservice.model.dto.RegistrationRequest;
import ens.etsmtl.ca.clientservice.model.dto.RegistrationResponse;
import ens.etsmtl.ca.clientservice.repository.ClientRepository;
import ens.etsmtl.ca.clientservice.service.exception.ConflictException;
import ens.etsmtl.ca.clientservice.service.exception.NotFoundException;
import ens.etsmtl.ca.clientservice.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ClientRegistrationService {

    private final ClientRepository clientRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final KycService kycService;

    public ClientRegistrationService(ClientRepository clientRepository,
            KeycloakAdminService keycloakAdminService,
            KycService kycService) {
        this.clientRepository = clientRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.kycService = kycService;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        if (clientRepository.existsByEmailIgnoreCaseOrSin(request.email(), request.sin())) {
            throw new ConflictException("A client already exists with this email or SIN");
        }

        String keycloakUserId = keycloakAdminService.createUser(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
        );

        Client client = new Client();
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setEmail(request.email().toLowerCase());
        client.setSin(request.sin());
        client.setPhone(request.phone());
        client.setKeycloakUserId(keycloakUserId);
        client.setStatus(ClientStatus.PENDING);
        client.setCreatedAt(OffsetDateTime.now());

        Client saved = clientRepository.save(client);
        return new RegistrationResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getStatus(),
                "Client registered as PENDING. Complete OTP setup in Keycloak, then confirm with OTP."
        );
    }

    @Transactional
    public RegistrationResponse confirm(RegistrationConfirmationRequest request) {
        Client client = clientRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("Client profile not found"));

        if (client.getStatus() == ClientStatus.ACTIVE) {
            return new RegistrationResponse(
                    client.getId(),
                    client.getEmail(),
                    client.getStatus(),
                    "Client already active"
            );
        }

        keycloakAdminService.verifyOtp(request.email(), request.password(), request.otp());

        if (!kycService.isEligibleForActivation(client.getSin())) {
            client.setStatus(ClientStatus.REJECTED);
            clientRepository.save(client);
            throw new ValidationException("KYC validation failed, profile rejected");
        }

        client.setStatus(ClientStatus.ACTIVE);
        client.setActivatedAt(OffsetDateTime.now());
        Client saved = clientRepository.save(client);

        return new RegistrationResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getStatus(),
                "Client profile activated"
        );
    }
}
