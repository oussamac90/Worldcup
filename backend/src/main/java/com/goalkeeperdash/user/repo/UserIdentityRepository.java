package com.goalkeeperdash.user.repo;

import com.goalkeeperdash.user.domain.AuthProvider;
import com.goalkeeperdash.user.domain.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    Optional<UserIdentity> findByProviderAndSubject(AuthProvider provider, String subject);

    List<UserIdentity> findByUserId(UUID userId);
}
