package de.kura_botics.project_t.service;

import de.kura_botics.project_t.entity.User;
import de.kura_botics.project_t.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for user management operations,
 * including secure user registration and password handling.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user in the system.
     * Ensures the username is unique and encrypts the password before saving.
     *
     * @param username    The desired username.
     * @param rawPassword The plain text password.
     * @param firstname   The user's first name.
     * @param lastname    The user's last name.
     * @return The newly saved User entity.
     * @throws IllegalArgumentException If the username is already taken.
     */
    public User registerNewUser(String username, String rawPassword, String firstname, String lastname) {
        logger.debug("Attempting to register new user with username: {}", username);

        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("Registration failed: Username '{}' is already in use.", username);
            throw new IllegalArgumentException("Username already in use");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setFirstname(firstname);
        newUser.setLastname(lastname);

        // The passwordEncoder bean (e.g., BCrypt) is automatically provided by the SecurityConfig
        newUser.setPassword(passwordEncoder.encode(rawPassword));

        User savedUser = userRepository.save(newUser);
        logger.info("Successfully registered new user: {}", savedUser.getUsername());

        return savedUser;
    }
}