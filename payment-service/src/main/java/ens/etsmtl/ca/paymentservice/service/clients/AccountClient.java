package ens.etsmtl.ca.paymentservice.service.clients;

import ens.etsmtl.ca.paymentservice.model.dto.InternalAccountResponse;
import ens.etsmtl.ca.paymentservice.service.exception.ExternalServiceException;
import ens.etsmtl.ca.paymentservice.service.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AccountClient {

    private final RestClient restClient;
    private final InternalAuthTokenProvider tokenProvider;
    private final String apiGatewayBaseUrl;

    public AccountClient(
            InternalAuthTokenProvider tokenProvider,
            @Value("${api-gateway.base-url}") String apiGatewayBaseUrl
    ) {
        this.restClient = RestClient.builder().build();
        this.tokenProvider = tokenProvider;
        this.apiGatewayBaseUrl = apiGatewayBaseUrl;
    }

    public InternalAccountResponse getAccount(Long accountId) {
        String url = apiGatewayBaseUrl + "/internal/api/v1/accounts/" + accountId;
        String accessToken = tokenProvider.fetchAccessToken();
        try {
            InternalAccountResponse response = restClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(InternalAccountResponse.class);
            if (response == null) {
                throw new ExternalServiceException("Account service returned empty response");
            }
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 400 || ex.getStatusCode().value() == 404) {
                throw new ValidationException("Account not found");
            }
            throw new ExternalServiceException("Account service unavailable: HTTP " + ex.getStatusCode().value());
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Account service unavailable");
        }
    }
}
