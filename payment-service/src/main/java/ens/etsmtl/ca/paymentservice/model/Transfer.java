package ens.etsmtl.ca.paymentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Transfer extends Transaction {

    @Column(nullable = false)
    private Long sourceAccountId;

    @Column
    private Long destinationAccountId;

    @Column
    private Long destinationClientId;

    @Column(length = 150)
    private String destinationEmail;

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public Long getDestinationClientId() {
        return destinationClientId;
    }

    public void setDestinationClientId(Long destinationClientId) {
        this.destinationClientId = destinationClientId;
    }

    public String getDestinationEmail() {
        return destinationEmail;
    }

    public void setDestinationEmail(String destinationEmail) {
        this.destinationEmail = destinationEmail;
    }
}
