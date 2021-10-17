package kika.repository;

import java.util.Collection;
import kika.domain.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    @Query("select TL from TaskList TL where TL.group.id=:groupId")
    Collection<TaskList> getTaskListsByGroupId(@Param("groupId") long groupId);
}
