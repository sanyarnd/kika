package kika.repository;

import kika.domain.Account;
import kika.domain.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.Optional;

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
