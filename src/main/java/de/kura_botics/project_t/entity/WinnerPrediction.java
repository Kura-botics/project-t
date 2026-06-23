package de.kura_botics.project_t.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a winner prediction in the database.
 * Each user can have one prediction on which team will win the current competition
 */
@Entity
@Table(name = "winner_predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinnerPrediction{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "winner_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable= false)
    private User user;

    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String team;

    @Column(name = "is_scored")
    private boolean isScored = false;
}