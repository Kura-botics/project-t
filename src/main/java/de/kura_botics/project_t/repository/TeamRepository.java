package de.kura_botics.project_t.repository;

import de.kura_botics.project_t.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Match entity operations.
 * Provides standard CRUD operations
 */
public interface TeamRepository extends JpaRepository<Team, String> {

}