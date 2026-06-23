package de.kura_botics.project_t.dto.api;

import java.util.List;

/**
 * Data Transfer Object representing the root response when fetching a list of matches from the API.
 *
 * @param matches A list containing the detailed data for each requested match.
 */
public record ApiMatchResponseDTO(List<ApiMatchDTO> matches) {
}