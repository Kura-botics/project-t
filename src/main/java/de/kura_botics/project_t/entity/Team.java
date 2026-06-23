package de.kura_botics.project_t.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a football team in the database.
 * Maps to the "team" table and holds data synced from the external API.
 * The data is used to give more information for the corresponding TLA.
 * Crest is not used for now, but it holds the flag of the team and can be used later on
 */
@Entity
@Table(name = "team")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @Column(name = "tla", nullable = false, columnDefinition = "CHAR(3)")
    private String tla;

    @Column(name = "name", nullable = false)
    private String name;

    @Column
    private String crest;

    @Column(name = "club_colors")
    private String clubColors;
}