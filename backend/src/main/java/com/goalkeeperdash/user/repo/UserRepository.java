package com.goalkeeperdash.user.repo;

import com.goalkeeperdash.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u where lower(u.displayName) like lower(concat('%', :q, '%')) order by u.createdAt desc")
    List<User> searchByDisplayName(String q);

    long countBySyntheticFalse();

    long countByCreatedAtAfter(Instant after);
}
