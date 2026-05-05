package se.scb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "E-post är obligatorisk")
    @Email(message = "Ogiltig e-postadress")
    private String email;

    @NotBlank(message = "Lösenord är obligatoriskt")
    private String password;
}
