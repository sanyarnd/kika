package kika.repository;

import kika.domain.AccountTaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AccountTaskAssigneeRepository extends JpaRepository<AccountTaskAssignee, AccountTaskAssignee.PK> {
}
