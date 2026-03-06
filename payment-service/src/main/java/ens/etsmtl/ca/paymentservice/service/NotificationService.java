package ens.etsmtl.ca.paymentservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public NotificationService(
            JavaMailSender mailSender,
            @Value("${notification.mail.from:no-reply@canbankx.local}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendTransferInvitation(String destinationEmail, Long transferId, BigDecimal amount) {
        if (destinationEmail == null || destinationEmail.isBlank()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(destinationEmail);
        message.setSubject("CanBankX - Transfer pending acceptance");
        message.setText("Transfer #" + transferId + " is pending acceptance. Amount: " + amount + " CAD.");
        mailSender.send(message);
    }
}
