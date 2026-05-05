package se.scb.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.scb.dto.AuthResponse;
import se.scb.dto.LoginRequest;
import se.scb.dto.RegisterRequest;
import se.scb.exception.EmailAlreadyExistsException;
import se.scb.model.Role;
import se.scb.model.User;
import se.scb.repository.UserRepository;
import se.scb.util.JwtUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailPublisher emailPublisher;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_givenNewEmail_savesUserAndReturnsResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.se");
        request.setPassword("secret123");
        request.setFirstName("Anna");
        request.setLastName("Svensson");

        when(userRepository.existsByEmail("new@test.se")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        User saved = new User("new@test.se", "hashed", "Anna", "Svensson");
        saved.setRole(Role.ROLE_USER);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        AuthResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("new@test.se");
        assertThat(response.getFirstName()).isEqualTo("Anna");
        verify(userRepository).save(any(User.class));
        verify(emailPublisher).publishEmailEvent("new@test.se", "Anna", "REGISTER");
    }

    @Test
    void register_givenDuplicateEmail_throwsEmailAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("taken@test.se");
        request.setPassword("secret123");
        request.setFirstName("Bo");
        request.setLastName("Berg");

        when(userRepository.existsByEmail("taken@test.se")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_givenDisabledAccount_throwsDisabledException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("disabled@test.se");
        request.setPassword("pass");

        User disabled = new User("disabled@test.se", "hashed", "Eve", "L");
        disabled.setRole(Role.ROLE_USER);
        disabled.setEnabled(false);

        when(userRepository.findByEmail("disabled@test.se")).thenReturn(Optional.of(disabled));

        assertThatThrownBy(() -> authService.login(request, null))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void login_givenBadCredentials_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.se");
        request.setPassword("wrongpassword");

        User user = new User("user@test.se", "hashed", "Lars", "K");
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);

        when(userRepository.findByEmail("user@test.se")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request, null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_givenValidCredentials_generatesTokenAndPublishesEvent() {
        LoginRequest request = new LoginRequest();
        request.setEmail("active@test.se");
        request.setPassword("correctpass");

        User user = new User("active@test.se", "hashed", "Karin", "Nilsson");
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);

        when(userRepository.findByEmail("active@test.se")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctpass", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        var mockResponse = mock(jakarta.servlet.http.HttpServletResponse.class);
        AuthResponse response = authService.login(request, mockResponse);

        assertThat(response.getEmail()).isEqualTo("active@test.se");
        assertThat(response.getFirstName()).isEqualTo("Karin");
        verify(emailPublisher).publishEmailEvent("active@test.se", "Karin", "LOGIN");
    }
}
