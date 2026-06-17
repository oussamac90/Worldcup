package com.goalkeeperdash.backoffice.security;

import com.goalkeeperdash.backoffice.repo.AdminAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads admin accounts for the back-office form-login chain. */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminAccountRepository repo;

    public AdminUserDetailsService(AdminAccountRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username)
                .map(a -> User.withUsername(a.getUsername())
                        .password(a.getPasswordHash())
                        .roles(a.getRole())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("No admin: " + username));
    }
}
