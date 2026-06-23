package de.kura_botics.project_t.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user in the database.
 * Maps to the "users" table and holds information about their name
 * and their password, which is of course encrypted
 */
@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable= false, unique= true, length = 50)
    private String username;

    @Column(nullable= false, length = 50)
    private String firstname;

    @Column(nullable= false, length = 50)
    private String lastname;

    @Column(nullable= false, length = 255)
    private String password;
}