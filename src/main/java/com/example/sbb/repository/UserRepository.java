package com.example.sbb.repository;

import com.example.sbb.entity.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {

    Optional<SiteUser> findByUsername(String username);
    Optional<SiteUser> findByEmail(String email);
}
