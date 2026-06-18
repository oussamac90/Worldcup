package com.goalkeeperdash.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Seeded, static reference data. Owned by the {@code user} module; exposed to
 * other modules via {@link com.goalkeeperdash.user.api.NationService}.
 */
@Entity
@Table(name = "nation")
@Getter
@Setter
@NoArgsConstructor
public class Nation {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    /** ISO 3166-1 alpha-3, e.g. MAR. */
    @Column(name = "code", length = 3, nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    /** CSV or JSON of hex colors for client rendering. */
    @Column(name = "flag_colors")
    private String flagColors;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Nation(UUID id, String code, String name, String flagColors, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.flagColors = flagColors;
        this.active = active;
    }
}
