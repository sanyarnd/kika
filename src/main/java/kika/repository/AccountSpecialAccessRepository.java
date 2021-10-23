package kika.repository;

import java.util.List;
import kika.domain.AccountSpecialAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AccountSpecialAccessRepository
    extends JpaRepository<AccountSpecialAccess, AccountSpecialAccess.PK> {
    @Query("select ASA from AccountSpecialAccess ASA where ASA.id.tasklistId=:taskListId")
    List<AccountSpecialAccess> getSpecialAccessAccountsByTaskListId(@Param("taskListId") long taskListId);

    @Query("select ASA from AccountSpecialAccess ASA "
        + "where ASA.id.accountId=:accountId and ASA.taskList.specialAccess.size=1")
    List<AccountSpecialAccess> getListsWithOneSpecialAccessAccountById(@Param("accountId") long accountId);
}
