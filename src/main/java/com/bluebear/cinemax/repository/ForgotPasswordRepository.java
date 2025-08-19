package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {

    Optional<ForgotPassword> findByAccount(Account account);

}
