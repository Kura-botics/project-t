package de.kura_botics.project_t.repository;

import de.kura_botics.project_t.entity.WinnerPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WinnerPrediction entity operations.
 * Provides standard CRUD operations and custom requests to find specific predictions.
 */
@Repository
public interface WinnerPredictionRepository extends JpaRepository<WinnerPrediction, Integer> {

    /**
     * Retrives all WinnerPredictions which have yet to be scored.
     *
     * @return A list of all unscored winner predictions.
     */
    List<WinnerPrediction> findByIsScoredFalse();

    /**
     * Finds a specific WinnerPrediction made by one specific user.
     *
     * @param id The unique ID of the user which made the winner prediction.
     * @return An Optional containing the winner prediction if found.
     */
    Optional<WinnerPrediction> findByUserId(Long id);
}