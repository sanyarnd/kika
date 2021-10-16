package kika.repository;

import kika.domain.AccountTaskSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AccountTaskSubscriberRepository extends JpaRepository<AccountTaskSubscriber, AccountTaskSubscriber.PK> {
}
