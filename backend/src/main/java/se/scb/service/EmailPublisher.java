package se.scb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.scb.dto.EmailEvent;

@Service
public class EmailPublisher {

    private static final Logger log = LoggerFactory.getLogger(EmailPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.email}")
    private String routingKey;

    public EmailPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEmailEvent(String email, String firstName, String eventType) {
        try {
            EmailEvent event = new EmailEvent(email, firstName, eventType);
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Email-event publicerat: {} till {}", eventType, email);
        } catch (Exception ex) {
            log.error("Misslyckades publicera email-event: {}", ex.getMessage());
        }
    }
}
