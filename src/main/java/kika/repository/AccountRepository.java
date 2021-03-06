package kika.repository;

import java.util.Optional;
import kika.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("select A from Account A where A.provider=:provider and A.providerId=:providerId")
    Optional<Account> findByProviderId(
        @Param("provider") Account.Provider provider,
        @Param("providerId") String providerId
    );

    @Query("select A from Account A where A.refreshToken=:token")
    Optional<Account> findByRefreshToken(@Param("token") String token);
}
