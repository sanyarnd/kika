package kika.repository;

import kika.domain.AccountRole;
import kika.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
@Transactional
public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("select AR from AccountRole AR left join Group G on AR.group.id=G.id where AR.account.id=:accountId")
    Collection<AccountRole> getAccountGroups(@Param("accountId") long accountId);
}
