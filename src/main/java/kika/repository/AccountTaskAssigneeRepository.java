package kika.repository;

import kika.domain.AccountTaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AccountTaskAssigneeRepository extends JpaRepository<AccountTaskAssignee, AccountTaskAssignee.PK> {
    @Query("select ATA from AccountTaskAssignee ATA where ATA.id.accountId=:accountId and ATA.id.taskId=:taskId")
    AccountTaskAssignee getAssigneeByIds(@Param("accountId") long accountId, @Param("taskId") long taskId);
}
