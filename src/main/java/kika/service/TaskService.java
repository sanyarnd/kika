package kika.service;

import kika.domain.*;
import kika.repository.*;
import kika.security.principal.KikaPrincipal;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final AccountRepository accountRepository;
    private final AccountTaskAssigneeRepository assigneeRepository;
    private final AccountTaskSubscriberRepository subscriberRepository;

    private final TaskListService taskListService;

    private void checkAccountAccess(Set<Account> accountsWithAccess, Account account, Task task) {
        if (!accountsWithAccess.contains(account)) {
            throw new IllegalArgumentException(
                String.format("Account id=%d has no access to the task list id=%d", account.safeId(),
                    task.getList().safeId()));
        }
    }

    private void checkTaskListAndParent(long listId, @NotNull Task parent) {
        if (parent.getList().safeId() != listId) {
            throw new IllegalArgumentException(String.format("Parent list id (%d) does not match child list id (%d)",
                parent.safeId(), listId));
        }
    }

    private void checkGroupMemberPermission(KikaPrincipal principal, Group group) {
        if (group.getMembers().stream()
            .noneMatch(member -> member.getAccount().safeId() == principal.accountId() &&
                member.getRole() != AccountRole.Role.RESTRICTED)) {
            throw new BadCredentialsException("Not enough group permissons");
        }
    }

    private void checkListAccess(KikaPrincipal principal, TaskList list) {
        if (!list.accountHasAccess(principal.accountId())) {
            throw new BadCredentialsException("No access");
        }
    }

    private void runAccessChecks(KikaPrincipal principal, TaskList list) {
        checkGroupMemberPermission(principal, list.getGroup());
        checkListAccess(principal, list);
    }

    @Transactional
    public long create(
        @NotNull String name,
        @Nullable String description,
        long listId,
        Long parentId,
        KikaPrincipal principal
    ) {
        TaskList list = taskListRepository.getById(listId);
        runAccessChecks(principal, list);
        Task parent = parentId == null ? null : taskRepository.getById(parentId);
        if (parent != null) {
            checkTaskListAndParent(listId, parent);
        }
        return taskRepository.save(new Task(name, description, parent, list)).safeId();
    }

    @Transactional
    public void rename(long id, @NotNull String name, KikaPrincipal principal) {
        Task task = taskRepository.getById(id);
        runAccessChecks(principal, task.getList());
        task.setName(name);
    }

    @Transactional
    public void setDescription(long id, @Nullable String description, KikaPrincipal principal) {
        Task task = taskRepository.getById(id);
        runAccessChecks(principal, task.getList());
        task.setDescription(description);
    }

    @Transactional
    public TaskDto get(long id, KikaPrincipal principal) {
        Task task = taskRepository.getById(id);
        checkListAccess(principal, task.getList());
        return new TaskDto(task.safeId(), task.getName(), task.getDescription(), task.getStatus(),
            task.getParentId(), task.getList().safeId(),
            task.getChildren().stream().map(AutoPersistable::safeId).collect(Collectors.toSet()));
    }

    @Transactional
    public void delete(long id, KikaPrincipal principal) {
        Task task = taskRepository.getById(id);
        runAccessChecks(principal, task.getList());
        taskRepository.delete(task);
    }

    @Transactional
    public void move(long taskId, @Nullable Long listId, @Nullable Long parentId, KikaPrincipal principal) {
        Task task = taskRepository.getById(taskId);
        runAccessChecks(principal, task.getList());
        if (listId != null && task.getList().safeId() != listId) {
            TaskList list = taskListRepository.getById(listId);
            task.setParent(null);
            task.setList(list);
            task.moveChildrenIntoList(list);
        }
        if (parentId != null) {
            Task parent = taskRepository.getById(parentId);
            checkTaskListAndParent(listId != null ? listId : task.getList().safeId(), parent);
            task.setParent(parent);
        }
    }

    @Transactional
    public void assign(long taskId, KikaPrincipal principal) {
        Task task = taskRepository.getById(taskId);
        checkListAccess(principal, task.getList());
        assigneeRepository.save(new AccountTaskAssignee(task, accountRepository.getById(principal.accountId())));
        subscriberRepository.save(new AccountTaskSubscriber(task, accountRepository.getById(principal.accountId())));
    }

    @Transactional
    public void retract(long taskId, KikaPrincipal principal) {
        Task task = taskRepository.getById(taskId);
        checkListAccess(principal, task.getList());
        assigneeRepository.delete(assigneeRepository.getAssigneeByIds(principal.accountId(), taskId));
    }

    @Transactional
    public Collection<Account> getAssignees(long taskId, KikaPrincipal principal) {
        Task task = taskRepository.getById(taskId);
        checkListAccess(principal, task.getList());
        return task.getAssignees().stream()
            .map(AccountTaskAssignee::getAccount).collect(Collectors.toSet());
    }

    @Transactional
    public void subscribe(long taskId, KikaPrincipal principal) {
        Task task = taskRepository.getById(taskId);
        checkListAccess(principal, task.getList());
        subscriberRepository.save(new AccountTaskSubscriber(task, accountRepository.getById(principal.accountId())));
    }

    @Transactional
    public void unsubscribe(long taskId, KikaPrincipal principal) {
        Task task = taskRepository.getById(taskId);
        checkListAccess(principal, task.getList());
        subscriberRepository.delete(subscriberRepository.getSubscriberByIds(principal.accountId(), taskId));
    }

    @Transactional
    public Collection<Account> getSubscribers(long taskId, KikaPrincipal principal) {
        Task task = taskRepository.getById(taskId);
        checkListAccess(principal, task.getList());
        return task.getSubscribers().stream()
            .map(AccountTaskSubscriber::getAccount)
            .collect(Collectors.toSet());
    }

    @Transactional
    public void setStatus(long id, @NotNull Task.Status status, KikaPrincipal principal) {
        Task task = taskRepository.getById(id);
        checkListAccess(principal, task.getList());
        task.setStatus(status);
    }
}
