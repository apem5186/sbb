package com.example.sbb.repository;

import com.example.sbb.entity.user.SiteUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {

    Optional<SiteUser> findByUsername(String username);
    Optional<SiteUser> findByEmail(String email);
    Optional<SiteUser> findByUsernameAndEmail(String username, String email);
    @Transactional
    @Modifying
    @Query("UPDATE SiteUser s SET s.password =:pw WHERE s.id =:id")
    void updateUserPassword(int id, String pw);
}
