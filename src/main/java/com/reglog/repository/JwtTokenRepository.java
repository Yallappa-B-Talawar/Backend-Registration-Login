package com.reglog.repository;

import com.reglog.entity.JwtToken;
import com.reglog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByToken(String token);

    @Transactional
    @Modifying
    void deleteByToken(String token);

    @Transactional
    @Modifying
    void deleteByUser(User user);

    @Transactional
    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime time);
}
