package ens.etsmtl.ca.clientservice.api;

import ens.etsmtl.ca.clientservice.api.dto.RegistrationConfirmationRequest;
import ens.etsmtl.ca.clientservice.api.dto.RegistrationRequest;
import ens.etsmtl.ca.clientservice.api.dto.RegistrationResponse;
import ens.etsmtl.ca.clientservice.service.ClientRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients/registrations")
public class ClientRegistrationController {

    private final ClientRegistrationService clientRegistrationService;

    public ClientRegistrationController(ClientRegistrationService clientRegistrationService) {
        this.clientRegistrationService = clientRegistrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        return clientRegistrationService.register(request);
    }

    @PostMapping("/confirm")
    public RegistrationResponse confirm(@Valid @RequestBody RegistrationConfirmationRequest request) {
        return clientRegistrationService.confirm(request);
    }
}
