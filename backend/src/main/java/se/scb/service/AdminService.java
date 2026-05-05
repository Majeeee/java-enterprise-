package se.scb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.scb.dto.UserResponse;
import se.scb.exception.ResourceNotFoundException;
import se.scb.model.User;
import se.scb.repository.UserRepository;

import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse enableUser(Long userId) {
        User user = findUserById(userId);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Admin aktiverade konto för: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
        log.info("Admin tog bort konto: {}", user.getEmail());
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Användare med id " + id + " hittades inte"));
    }
}
