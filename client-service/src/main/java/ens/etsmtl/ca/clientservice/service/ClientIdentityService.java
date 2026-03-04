package ens.etsmtl.ca.clientservice.service;

import ens.etsmtl.ca.clientservice.model.Client;
import ens.etsmtl.ca.clientservice.model.ClientStatus;
import ens.etsmtl.ca.clientservice.model.dto.LoginChallengeRequest;
import ens.etsmtl.ca.clientservice.model.dto.LoginChallengeResponse;
import ens.etsmtl.ca.clientservice.model.dto.LoginRequest;
import ens.etsmtl.ca.clientservice.model.dto.LoginResponse;
import ens.etsmtl.ca.clientservice.repository.ClientRepository;
import ens.etsmtl.ca.clientservice.service.exception.NotFoundException;
import ens.etsmtl.ca.clientservice.service.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientIdentityService {

    private static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);

    private final ClientRepository clientRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final Map<String, ChallengeContext> pendingChallenges = new ConcurrentHashMap<>();

    public ClientIdentityService(ClientRepository clientRepository,
            KeycloakAdminService keycloakAdminService) {
        this.clientRepository = clientRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    public LoginChallengeResponse startLoginChallenge(LoginRequest request) {
        Client client = clientRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (client.getStatus() != ClientStatus.ACTIVE) {
            throw new UnauthorizedException("Client is not active");
        }

        String challengeId = UUID.randomUUID().toString();
        pendingChallenges.put(challengeId, new ChallengeContext(
                client.getId(),
                client.getEmail(),
                client.getFirstName(),
                client.getLastName(),
                request.password(),
                OffsetDateTime.now()
        ));

        return new LoginChallengeResponse(
                challengeId,
                true,
                "OTP challenge created. Submit OTP with challengeId."
        );
    }

    public LoginResponse completeLoginChallenge(LoginChallengeRequest request) {
        ChallengeContext challenge = pendingChallenges.get(request.challengeId());
        if (challenge == null) {
            throw new UnauthorizedException("Invalid challengeId");
        }
        if (challenge.createdAt().plus(CHALLENGE_TTL).isBefore(OffsetDateTime.now())) {
            pendingChallenges.remove(request.challengeId());
            throw new UnauthorizedException("Challenge expired");
        }

        Map<String, Object> tokens = keycloakAdminService.authenticateWithOtp(
                challenge.email(),
                challenge.password(),
                request.otp()
        );
        pendingChallenges.remove(request.challengeId());

        return new LoginResponse(
                challenge.clientId(),
                challenge.email(),
                challenge.firstName(),
                challenge.lastName(),
                toString(tokens.get("access_token")),
                toString(tokens.get("refresh_token")),
                toString(tokens.getOrDefault("token_type", "Bearer")),
                toLong(tokens.getOrDefault("expires_in", 0)),
                toString(tokens.get("scope"))
        );
    }

    private static String toString(Object value) {
        return value == null ? null : value.toString();
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private record ChallengeContext(
            Long clientId,
            String email,
            String firstName,
            String lastName,
            String password,
            OffsetDateTime createdAt
    ) {
    }
}
