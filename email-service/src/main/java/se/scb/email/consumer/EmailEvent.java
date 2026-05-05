package se.scb.email.consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent implements Serializable {
    private String toEmail;
    private String firstName;
    private String eventType;
}
