package com.goalkeeperdash.user.repo;

import com.goalkeeperdash.user.domain.Nation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NationRepository extends JpaRepository<Nation, UUID> {

    Optional<Nation> findByCode(String code);

    List<Nation> findAllByActiveTrueOrderByNameAsc();

    boolean existsByCode(String code);
}
