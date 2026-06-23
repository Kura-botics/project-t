package de.kura_botics.project_t.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing the current score in the database.
 * Maps to the "seasonal_score" table and holds information for each user on how
 * many points they have and how the score is put togheter
 * cww = correct world cup winner, cg = correct guess, cgd = correct goal difference, cw = correct winner
 */
@Entity
@Table(name = "seasonal_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalScore {
    @Id
    private Long id;

    @OneToOne(fetch= FetchType.LAZY)
    @MapsId //use userid as primary key
    @JoinColumn(name = "user_id", nullable= false)
    private User user;

    @Column(name = "total_points",nullable= false)
    private int totalPoints = 0;

    @Column(nullable= false)
    private int cww = 0;

    @Column(nullable= false)
    private int cg = 0;

    @Column(nullable= false)
    private int cgd = 0;

    @Column(nullable= false)
    private int cw = 0;
}