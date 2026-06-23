package de.kura_botics.project_t.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing the all-time score in the database
 * Has no use as of now but will be implemented later on.
 */
@Entity
@Table(name = "all_time_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllTimeScore{

    @Id
    private Integer id;

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