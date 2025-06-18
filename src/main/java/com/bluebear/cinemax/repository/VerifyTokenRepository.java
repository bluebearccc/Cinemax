package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.VerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerifyTokenRepository extends JpaRepository<VerifyToken, Integer> {

    public Optional<VerifyToken> findByEmail(String email);

    public void deleteByEmail(String email);

    public Optional<VerifyToken> findByToken(String token);
}
