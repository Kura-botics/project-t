package de.kura_botics.project_t.dto.api;

/**
 * Data Transfer Object representing the scoring information of a match.
 *
 * @param fullTime The DTO containing the final score at the end of standard playtime.
 */
public record ApiScoreDTO(ApiFullTimeDTO fullTime) {
}