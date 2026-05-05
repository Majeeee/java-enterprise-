package se.scb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Förnamn är obligatoriskt")
    @Size(min = 2, max = 50, message = "Förnamn måste vara mellan 2 och 50 tecken")
    private String firstName;

    @NotBlank(message = "Efternamn är obligatoriskt")
    @Size(min = 2, max = 50, message = "Efternamn måste vara mellan 2 och 50 tecken")
    private String lastName;

    @NotBlank(message = "E-post är obligatorisk")
    @Email(message = "Ogiltig e-postadress")
    private String email;

    @NotBlank(message = "Lösenord är obligatoriskt")
    @Size(min = 8, message = "Lösenord måste vara minst 8 tecken")
    private String password;
}
