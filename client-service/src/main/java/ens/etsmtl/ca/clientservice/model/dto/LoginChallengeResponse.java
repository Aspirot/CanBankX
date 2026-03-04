package ens.etsmtl.ca.clientservice.model.dto;

public record LoginChallengeResponse(
        String challengeId,
        boolean mfaRequired,
        String message
) {
}
