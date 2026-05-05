package se.scb.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSenderService {

    private static final Logger log = LoggerFactory.getLogger(MailSenderService.class);

    private final JavaMailSender mailSender;

    public MailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Välkommen till SCB Flyttningsstatistik");
        message.setText(String.format("""
                Hej %s,

                Tack för att du registrerade ett konto hos SCB Flyttningsstatistik.

                Ditt konto behöver aktiveras av en administratör innan du kan logga in.
                Du får ett nytt email när kontot är aktiverat.

                Med vänliga hälsningar,
                SCB-teamet
                """, firstName));

        mailSender.send(message);
        log.info("Välkomstmail skickat till: {}", toEmail);
    }

    public void sendLoginNotification(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Ny inloggning på ditt SCB-konto");
        message.setText(String.format("""
                Hej %s,

                Vi registrerade nyss en inloggning på ditt konto.

                Om det inte var du, kontakta en administratör omedelbart.

                Med vänliga hälsningar,
                SCB-teamet
                """, firstName));

        mailSender.send(message);
        log.info("Inloggningsnotis skickad till: {}", toEmail);
    }
}
