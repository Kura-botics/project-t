package de.kura_botics.project_t.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.kura_botics.project_t.entity.Match;

/**
 * Repository interface for Match entity operations.
 * Provides standard CRUD operations and custom queries for match scheduling and synchronization.
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {

    /**
     * Finds a specific match based on its external API identifier.
     *
     * @param apiMatchId The unique ID provided by the external football API.
     * @return An Optional containing the match if found.
     */
    Optional<Match> findByApiMatchId(Integer apiMatchId);

    /**
     * Retrieves all matches that have already kicked off but have not yet been scored in the system.
     *
     * @param now The current timestamp to compare against the kickoff dates.
     * @return A list of matches pending score evaluation.
     */
    @Query("SELECT m FROM Match m WHERE m.kickoffDate < :now AND m.isScored = false")
    List<Match> findUnscoredMatchesInPast(@Param("now") Instant now);
}