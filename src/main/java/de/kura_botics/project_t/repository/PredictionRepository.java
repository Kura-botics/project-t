package de.kura_botics.project_t.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.kura_botics.project_t.entity.Match;
import de.kura_botics.project_t.entity.Prediction;

/**
 * Repository interface for Prediction entity operations.
 * Provides standard CRUD operations and Lists for match predictions
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Integer>{

    /**
     * Finds all predictions for one match
     *
     * @param match The match used to find all predictions.
     * @return A list of all predictions for one game.
     */
    List<Prediction> findByMatch (Match match);
}