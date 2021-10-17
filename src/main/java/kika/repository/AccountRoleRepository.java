package kika.repository;

import kika.domain.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AccountRoleRepository extends JpaRepository<AccountRole, AccountRole.PK> {
}
