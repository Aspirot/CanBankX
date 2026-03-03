package ens.etsmtl.ca.clientservice.service;

import org.springframework.stereotype.Service;

@Service
public class KycService {

    public boolean isEligibleForActivation(String sin) {
        int checksum = 0;
        for (char c : sin.toCharArray()) {
            checksum += Character.getNumericValue(c);
        }
        return checksum % 2 == 0;
    }
}
