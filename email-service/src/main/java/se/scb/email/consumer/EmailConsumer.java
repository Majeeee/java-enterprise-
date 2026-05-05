package se.scb.email.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import se.scb.email.service.MailSenderService;

@Component
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    private final MailSenderService mailSenderService;

    public EmailConsumer(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void handleEmailEvent(EmailEvent event) {
        log.info("Email-event mottaget: {} för {}", event.getEventType(), event.getToEmail());
        try {
            switch (event.getEventType()) {
                case "REGISTER" -> mailSenderService.sendWelcomeEmail(
                        event.getToEmail(), event.getFirstName());
                case "LOGIN" -> mailSenderService.sendLoginNotification(
                        event.getToEmail(), event.getFirstName());
                default -> log.warn("Okänd eventtyp: {}", event.getEventType());
            }
        } catch (Exception ex) {
            log.error("Misslyckades skicka email till {}: {}", event.getToEmail(), ex.getMessage());
        }
    }
}
