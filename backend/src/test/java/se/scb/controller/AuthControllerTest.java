package se.scb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import se.scb.config.JwtAuthFilter;
import se.scb.dto.AuthResponse;
import se.scb.dto.LoginRequest;
import se.scb.dto.RegisterRequest;
import se.scb.service.AuthService;
import se.scb.util.JwtUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean JwtUtil jwtUtil;

    @Test
    void register_validRequest_returns201WithMessage() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@test.se");
        req.setPassword("secret123");
        req.setFirstName("Anna");
        req.setLastName("Berg");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse(
                        "Registrering lyckades. Vänta på att en admin aktiverar ditt konto.",
                        "ROLE_USER", "new@test.se", "Anna"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@test.se"))
                .andExpect(jsonPath("$.firstName").value("Anna"));
    }

    @Test
    void login_validCredentials_returns200AndSetsJwtCookie() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("active@test.se");
        req.setPassword("correctpass");

        doAnswer(inv -> {
            HttpServletResponse response = inv.getArgument(1);
            Cookie cookie = new Cookie("jwt", "mocked-jwt-token");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            return new AuthResponse("Inloggning lyckades", "ROLE_USER", "active@test.se", "Karin");
        }).when(authService).login(any(LoginRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("active@test.se"))
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().httpOnly("jwt", true));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.se");
        req.setPassword("wrongpass");

        when(authService.login(any(LoginRequest.class), any(HttpServletResponse.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Felaktig e-post eller lösenord"));
    }

    @Test
    void register_missingFields_returns400WithValidationErrors() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("not-an-email");
        // firstName, lastName, password omitted

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
