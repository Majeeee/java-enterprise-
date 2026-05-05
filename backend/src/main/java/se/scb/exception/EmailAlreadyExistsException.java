package se.scb.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("E-postadressen är redan registrerad: " + email);
    }
}
