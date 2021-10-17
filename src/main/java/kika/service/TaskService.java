package kika.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import kika.domain.Account;
import kika.domain.AccountTaskAssignee;
import kika.domain.AccountTaskSubscriber;
import kika.domain.AutoPersistable;
import kika.domain.Task;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountTaskAssigneeRepository;
import kika.repository.AccountTaskSubscriberRepository;
import kika.repository.TaskListRepository;
import kika.repository.TaskRepository;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final AccountRepository accountRepository;
    private final AccountTaskAssigneeRepository accountTaskAssigneeRepository;
    private final AccountTaskSubscriberRepository accountTaskSubscriberRepository;

    private final TaskListService taskListService;

    @Transactional
    public long create(@NotNull String name, @Nullable String description, long listId, Long parentId) {
        Task parent = parentId == null ? null : taskRepository.getById(parentId);
        if(parent != null && parent.getList().safeId() != listId) {
            throw new IllegalArgumentException();
        }
        return taskRepository.save(new Task(name, description, parent, taskListRepository.getById(listId))).safeId();
    }

    @Transactional
    public void rename(long id, @NotNull String name) {
        taskRepository.getById(id).setName(name);
    }

    @Transactional
    public void setDescription(long id, @Nullable String description) {
        taskRepository.getById(id).setDescription(description);
    }

    @Transactional
    public TaskDto get(long id) {
        Task task = taskRepository.getById(id);
        return new TaskDto(task.safeId(), task.getName(), task.getDescription(), task.getStatus(),
            task.getParentId(), task.getList().safeId(),
            task.getChildren().stream().map(AutoPersistable::safeId).collect(Collectors.toSet()));
    }

    @Transactional
    public void delete(long id) {
        taskRepository.deleteById(id);
    }

    @Transactional
    public void move(long taskId, @Nullable Long listId, @Nullable Long parentId) {
        Task task = taskRepository.getById(taskId);
        if (listId != null && task.getList().safeId() != listId) {
            TaskList list = taskListRepository.getById(listId);
            task.setParent(null);
            task.setList(list);
            task.moveChildrenIntoList(list);
        }
        if (parentId != null) {
            Task parent = taskRepository.getById(parentId);
            if(parent.getList().safeId() != task.getList().safeId()) {
                throw new IllegalArgumentException(
                        String.format("parent list id=%d, child list id=%d", parent.getList().safeId(), task.getList().safeId()));
            }
            task.setParent(parent);
        }
    }

    @Transactional
    public void assign(long taskId, @NotNull Collection<Long> assigneeIds) {
        Task task = taskRepository.getById(taskId);
        task.getAssignees().clear();
        Set<Account> accountsWithAccess = new HashSet<>(taskListService.getAccountsWithAccess(task.getList().safeId()));
        assigneeIds.stream()
                .map(accountRepository::getById)
                .forEach(account -> {
                    if(!accountsWithAccess.contains(account)) {
                        throw new IllegalArgumentException();
                    }
                    accountTaskAssigneeRepository.save(new AccountTaskAssignee(task, account));
                });
    }

    @Transactional
    public Collection<Account> getAssignees(long taskId) {
        return taskRepository.getById(taskId).getAssignees().stream()
                .map(AccountTaskAssignee::getAccount).collect(Collectors.toSet());
    }

    @Transactional
    public void subscribe(long taskId, @NotNull Collection<Long> subscriberIds) {
        Task task = taskRepository.getById(taskId);
        task.getSubscribers().clear();
        Set<Account> accountsWithAccess = new HashSet<>(taskListService.getAccountsWithAccess(task.getList().safeId()));
        subscriberIds.stream()
                .map(accountRepository::getById)
                .forEach(account -> {
                    if(!accountsWithAccess.contains(account)) {
                        throw new IllegalArgumentException();
                    }
                    accountTaskSubscriberRepository.save(new AccountTaskSubscriber(task, account));
                });
    }

    @Transactional
    public Collection<Account> getSubscribers(long taskId) {
        return taskRepository.getById(taskId).getSubscribers().stream()
            .map(AccountTaskSubscriber::getAccount)
            .collect(Collectors.toSet());
    }

    @Transactional
    public void setStatus(long id, @NotNull Task.Status status) {
        taskRepository.getById(id).setStatus(status);
    }
}
