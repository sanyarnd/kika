package kika.repository;

import kika.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
@Transactional
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select T from Task T where T.list.id=:id")
    Collection<Task> getTasksByTaskListId(@Param("id") long id);
}
