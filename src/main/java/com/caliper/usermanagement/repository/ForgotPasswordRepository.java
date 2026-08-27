package com.caliper.usermanagement.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.caliper.usermanagement.entity.ForgotPassword;


public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {

    Optional<ForgotPassword> getForgotPasswordByUserId(String userId);

    Optional<ForgotPassword> findByTokenHash(String tokenHash);
}
