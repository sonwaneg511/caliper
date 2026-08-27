package com.caliper.usermanagement.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.caliper.usermanagement.entity.EmailVerificationToken;


public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByUserId(String userId);

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
}
