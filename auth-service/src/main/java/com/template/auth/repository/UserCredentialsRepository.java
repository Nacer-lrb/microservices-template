package com.template.auth.repository;

import com.template.auth.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByUsername(String username);
    Optional<UserCredentials> findByEmail(String email);
}
