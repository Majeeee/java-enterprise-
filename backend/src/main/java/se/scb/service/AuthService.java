package se.scb.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.scb.dto.AuthResponse;
import se.scb.dto.LoginRequest;
import se.scb.dto.RegisterRequest;
import se.scb.exception.EmailAlreadyExistsException;
import se.scb.model.User;
import se.scb.repository.UserRepository;
import se.scb.util.JwtUtil;

@Service
public class AuthService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailPublisher emailPublisher;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, EmailPublisher emailPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailPublisher = emailPublisher;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName()
        );

        userRepository.save(user);
        log.info("Ny användare registrerad: {} (isEnabled=false)", user.getEmail());

        emailPublisher.publishEmailEvent(user.getEmail(), user.getFirstName(), "REGISTER");

        return new AuthResponse(
                "Registrering lyckades. Vänta på att en admin aktiverar ditt konto.",
                user.getRole().name(),
                user.getEmail(),
                user.getFirstName()
        );
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Felaktig e-post eller lösenord"));

        if (!user.isEnabled()) {
            throw new DisabledException("Kontot är inte aktiverat än. Kontakta en administratör.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Felaktig e-post eller lösenord");
        }

        String token = jwtUtil.generateToken(user);

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        log.info("Användare loggade in: {}", user.getEmail());
        emailPublisher.publishEmailEvent(user.getEmail(), user.getFirstName(), "LOGIN");

        return new AuthResponse(
                "Inloggning lyckades",
                user.getRole().name(),
                user.getEmail(),
                user.getFirstName()
        );
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.info("Användare loggade ut");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Användare hittades inte med e-post: " + email));
    }
}
