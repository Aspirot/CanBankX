package ens.etsmtl.ca.paymentservice.service.clients;

import ens.etsmtl.ca.paymentservice.model.dto.LedgerEntryRequest;
import ens.etsmtl.ca.paymentservice.service.exception.ExternalServiceException;
import ens.etsmtl.ca.paymentservice.service.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class LedgerClient {

    private final RestClient restClient;
    private final InternalAuthTokenProvider tokenProvider;
    private final String apiGatewayBaseUrl;

    public LedgerClient(
            InternalAuthTokenProvider tokenProvider,
            @Value("${api-gateway.base-url}") String apiGatewayBaseUrl
    ) {
        this.restClient = RestClient.builder().build();
        this.tokenProvider = tokenProvider;
        this.apiGatewayBaseUrl = apiGatewayBaseUrl;
    }

    public void appendEntry(LedgerEntryRequest request) {
        String url = apiGatewayBaseUrl + "/internal/api/v1/ledger/entries";
        String accessToken = tokenProvider.fetchAccessToken();
        try {
            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 400) {
                throw new ValidationException("Ledger rejected transfer: " + ex.getResponseBodyAsString());
            }
            throw new ExternalServiceException("Ledger service unavailable: HTTP " + ex.getStatusCode().value());
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException("Ledger service unavailable");
        }
    }
}
