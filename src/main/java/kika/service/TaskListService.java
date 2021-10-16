package kika.service;

import kika.domain.*;
import kika.repository.*;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskListService {
    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final AccountRepository accountRepository;
    private final AccountSpecialAccessRepository accountSpecialAccessRepository;

    @Transactional
    public long create(String name, Long parentId, long groupId) {
        TaskList parent = parentId == null ? null : taskListRepository.getById(parentId);
        return taskListRepository.save(new TaskList(name, parent,
                groupRepository.getById(groupId))).safeId();
    }

    @Transactional
    public void rename(long id, String name) {
        taskListRepository.getById(id).setName(name);
    }

    @Transactional
    public TaskList get(long id) {
        return taskListRepository.findById(id).orElse(null);
    }

    @Transactional
    public Collection<Account> getAccountsWithAccess(long id) {
        Set<Account> accountsWithSpecialAccess =
                accountSpecialAccessRepository.getSpecialAccessAccountsByTaskListId(id).stream()
                .map(AccountSpecialAccess::getAccount)
                .collect(Collectors.toSet());
        if(!accountsWithSpecialAccess.isEmpty()) {
            return accountsWithSpecialAccess;
        } else {
            TaskList taskList = taskListRepository.findById(id).orElseThrow();
            if(taskList.getParent() != null) {
                accountsWithSpecialAccess = new HashSet<>(getAccountsWithAccess(taskList.getParentId()));
                if(!accountsWithSpecialAccess.isEmpty()) {
                    return accountsWithSpecialAccess;
                }
            }
            return taskListRepository.getById(id).getGroup().getMembers().stream()
                    .map(AccountRole::getAccount)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public void setSpecialAccess(long id, Collection<Long> membersWithAccessIds) {
        TaskList taskList = taskListRepository.getById(id);
        if(!membersWithAccessIds.isEmpty()) {
            Set<Account> accountsWithPotentialAccess = new HashSet<>(getAccountsWithAccess(id));
            // The new access set must be a subset of the parent access set
            if (!accountsWithPotentialAccess.stream()
                    .map(Account::safeId)
                    .collect(Collectors.toSet())
                    .containsAll(membersWithAccessIds)) {
                throw new IllegalArgumentException();
            }
            // Recursively revoke access from accounts from this list and all its children
            taskList.getSpecialAccess().stream()
                    .filter(accSpAcc -> membersWithAccessIds.contains(accSpAcc.getAccount().safeId()))
                    .forEach(accSpAcc -> taskList.revokeSpecialAccessRecursively(accSpAcc.getAccount().safeId()));
            // Add access to accounts that do not have it yet
            Set<Long> currentAccessIds = taskList.getSpecialAccess().stream()
                    .map(accSpAcc -> accSpAcc.getAccount().safeId())
                    .collect(Collectors.toSet());
            membersWithAccessIds.stream()
                    .filter(accId -> !currentAccessIds.contains(accId))
                    .map(accountRepository::getById)
                    .forEach(account -> accountSpecialAccessRepository.save(new AccountSpecialAccess(taskList, account)));

        } else {
            // If the new special access set is blank (= remove all access restrictions),
            // then clearing it won't affect child lists' access restrictions
            taskList.getSpecialAccess().clear();
        }
    }

    @Transactional
    public void delete(long id) {
        taskListRepository.deleteById(id);
    }

    @Transactional
    public Set<TaskDto> getTasks(long id) {
        return taskRepository.getTasksByTaskListId(id).stream()
                .map(task -> new TaskDto(task.safeId(), task.getName(), task.getDescription(), task.getStatus(),
                        task.getParentId(), task.getList().safeId(),
                        task.getChildren().stream().map(AutoPersistable::safeId).collect(Collectors.toSet())))
                .collect(Collectors.toSet());
    }
}
