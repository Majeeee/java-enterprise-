package se.scb.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.scb.dto.UserResponse;
import se.scb.exception.ResourceNotFoundException;
import se.scb.model.Role;
import se.scb.model.User;
import se.scb.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void enableUser_givenExistingUser_setsEnabledAndReturnsUpdatedResponse() {
        User user = new User("user@test.se", "hashed", "Lars", "Ek");
        user.setRole(Role.ROLE_USER);
        user.setEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse result = adminService.enableUser(1L);

        assertThat(result.isEnabled()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_givenExistingUser_deletesFromRepository() {
        User user = new User("todelete@test.se", "hashed", "Eva", "Berg");
        user.setRole(Role.ROLE_USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        adminService.deleteUser(2L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_givenMissingUser_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void getAllUsers_returnsMappedResponses() {
        User u1 = new User("a@test.se", "h", "Alice", "A");
        u1.setRole(Role.ROLE_USER);
        User u2 = new User("b@test.se", "h", "Bob", "B");
        u2.setRole(Role.ROLE_ADMIN);

        when(userRepository.findAll()).thenReturn(java.util.List.of(u1, u2));

        var result = adminService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("a@test.se");
        assertThat(result.get(1).getEmail()).isEqualTo("b@test.se");
    }
}
