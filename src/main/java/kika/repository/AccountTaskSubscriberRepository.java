package kika.repository;

import kika.domain.AccountTaskSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AccountTaskSubscriberRepository
    extends JpaRepository<AccountTaskSubscriber, AccountTaskSubscriber.PK> {
    @Query("select ATS from AccountTaskSubscriber ATS where ATS.id.accountId=:accountId and ATS.id.taskId=:taskId")
    AccountTaskSubscriber getSubscriberByIds(@Param("accountId") long accountId, @Param("taskId") long taskId);
}
