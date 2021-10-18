package kika.repository;

import kika.domain.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
}
