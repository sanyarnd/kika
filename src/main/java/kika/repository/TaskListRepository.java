package kika.repository;

import kika.domain.TaskList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    @Query("select TL from TaskList TL where TL.group.id=:groupId")
    List<TaskList> getTaskListsByGroupId(@Param("groupId") long groupId);

//    List<TaskList> findByGroupAndAccount(long groupId, long accountId);
}
