package ens.etsmtl.ca.clientservice.api;

import ens.etsmtl.ca.clientservice.model.dto.LoginChallengeRequest;
import ens.etsmtl.ca.clientservice.model.dto.LoginChallengeResponse;
import ens.etsmtl.ca.clientservice.model.dto.LoginRequest;
import ens.etsmtl.ca.clientservice.model.dto.LoginResponse;
import ens.etsmtl.ca.clientservice.service.ClientIdentityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients/login")
public class ClientIdentityController {

    private final ClientIdentityService clientIdentityService;

    public ClientIdentityController(ClientIdentityService clientIdentityService) {
        this.clientIdentityService = clientIdentityService;
    }

    @PostMapping
    public LoginChallengeResponse login(@Valid @RequestBody LoginRequest request) {
        return clientIdentityService.startLoginChallenge(request);
    }

    @PostMapping("/challenge")
    public LoginResponse challenge(@Valid @RequestBody LoginChallengeRequest request) {
        return clientIdentityService.completeLoginChallenge(request);
    }
}
