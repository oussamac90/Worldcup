package com.goalkeeperdash.backoffice.security;

import com.goalkeeperdash.backoffice.domain.AdminAccount;
import com.goalkeeperdash.backoffice.repo.AdminAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages admin accounts, including bootstrap creation from env at boot (§7.1). */
@Service
public class AdminAccountService {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountService.class);

    private final AdminAccountRepository repo;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(AdminAccountRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    /** Insert-if-absent (idempotent) — safe to call on every boot. */
    @Transactional
    public void ensureBootstrapAdmin(String username, String rawPassword) {
        if (repo.existsByUsername(username)) {
            return;
        }
        AdminAccount admin = new AdminAccount();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setRole("ADMIN");
        repo.save(admin);
        log.info("Bootstrapped admin account '{}'", username);
    }
}
