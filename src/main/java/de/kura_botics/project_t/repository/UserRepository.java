package de.kura_botics.project_t.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.kura_botics.project_t.entity.User;

/**
 * Repository interface for User entity operations.
 * Provides standard CRUD operations and a search for a username
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a specific match based on a username
     *
     * @param username The unique username given to look for
     * @return An Optional containing the user if provided
     */
    Optional<User> findByUsername(String username);
}