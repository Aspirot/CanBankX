package ens.etsmtl.ca.clientservice.service;

import ens.etsmtl.ca.clientservice.api.dto.RegistrationConfirmationRequest;
import ens.etsmtl.ca.clientservice.api.dto.RegistrationRequest;
import ens.etsmtl.ca.clientservice.api.dto.RegistrationResponse;
import ens.etsmtl.ca.clientservice.domain.ClientProfile;
import ens.etsmtl.ca.clientservice.domain.ClientStatus;
import ens.etsmtl.ca.clientservice.repository.ClientProfileRepository;
import ens.etsmtl.ca.clientservice.service.exception.ConflictException;
import ens.etsmtl.ca.clientservice.service.exception.NotFoundException;
import ens.etsmtl.ca.clientservice.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ClientRegistrationService {

    private final ClientProfileRepository clientProfileRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final KycService kycService;

    public ClientRegistrationService(
            ClientProfileRepository clientProfileRepository,
            KeycloakAdminService keycloakAdminService,
            KycService kycService
    ) {
        this.clientProfileRepository = clientProfileRepository;
        this.keycloakAdminService = keycloakAdminService;
        this.kycService = kycService;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        if (clientProfileRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("A client profile already exists for this email");
        }
        if (clientProfileRepository.existsBySin(request.sin())) {
            throw new ConflictException("A client profile already exists for this SIN");
        }

        String keycloakUserId = keycloakAdminService.createUser(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
        );

        ClientProfile profile = new ClientProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setEmail(request.email().toLowerCase());
        profile.setSin(request.sin());
        profile.setPhone(request.phone());
        profile.setKeycloakUserId(keycloakUserId);
        profile.setStatus(ClientStatus.PENDING);
        profile.setCreatedAt(OffsetDateTime.now());

        ClientProfile saved = clientProfileRepository.save(profile);
        return new RegistrationResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getStatus(),
                "Client registered as PENDING. Complete OTP setup in Keycloak, then confirm with OTP."
        );
    }

    @Transactional
    public RegistrationResponse confirm(RegistrationConfirmationRequest request) {
        ClientProfile profile = clientProfileRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("Client profile not found"));

        if (profile.getStatus() == ClientStatus.ACTIVE) {
            return new RegistrationResponse(
                    profile.getId(),
                    profile.getEmail(),
                    profile.getStatus(),
                    "Client already active"
            );
        }

        keycloakAdminService.verifyOtp(request.email(), request.password(), request.otp());

        if (!kycService.isEligibleForActivation(profile.getSin())) {
            profile.setStatus(ClientStatus.REJECTED);
            clientProfileRepository.save(profile);
            throw new ValidationException("KYC validation failed, profile rejected");
        }

        profile.setStatus(ClientStatus.ACTIVE);
        profile.setActivatedAt(OffsetDateTime.now());
        ClientProfile saved = clientProfileRepository.save(profile);

        return new RegistrationResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getStatus(),
                "Client profile activated"
        );
    }
}
