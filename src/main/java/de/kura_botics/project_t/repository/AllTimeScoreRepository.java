package de.kura_botics.project_t.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.kura_botics.project_t.entity.AllTimeScore;

/**
 * Repository interface for AllTimeScore entity operations.
 * Provides standard CRUD operations.
 */
@Repository
public interface AllTimeScoreRepository extends JpaRepository<AllTimeScore, Long>{

}