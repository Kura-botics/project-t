package de.kura_botics.project_t.repository;

import de.kura_botics.project_t.entity.SeasonalScore;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for SeasonalScore entity operations.
 * Provides standard CRUD operations.
 */
public interface SeasonalScoreRepository extends JpaRepository<SeasonalScore, Long> {
}