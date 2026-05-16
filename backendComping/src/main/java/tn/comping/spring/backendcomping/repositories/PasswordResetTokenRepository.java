package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.PasswordResetToken;

import java.util.Optional;
import java.util.List;
import java.util.Date;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findByUserId(String userId);

    List<PasswordResetToken> findByUserIdAndUsedFalseAndExpiryDateAfter(String userId, Date now);

}

