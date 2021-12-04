package kika.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import kika.controller.request.EditTaskRequest;
import kika.controller.response.GetTaskEditInfoResponse;
import kika.controller.response.GetTaskInfoResponse;
import kika.domain.Account;
import kika.domain.AccountRole;
import kika.domain.AccountTaskAssignee;
import kika.domain.AccountTaskSubscriber;
import kika.domain.AutoPersistable;
import kika.domain.Group;
import kika.domain.Task;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountTaskAssigneeRepository;
import kika.repository.AccountTaskSubscriberRepository;
import kika.repository.TaskListRepository;
import kika.repository.TaskRepository;
import kika.security.principal.KikaPrincipal;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final AccountRepository accountRepository;
    private final AccountTaskAssigneeRepository assigneeRepository;
    private final AccountTaskSubscriberRepository subscriberRepository;

    private List<TaskDto> getFullTaskTree(Task task) {
        if (task.getChildren().isEmpty()) {
            return List.of();
        }
        return task.getChildren().stream()
            .sorted(Comparator.comparingLong(AutoPersistable::safeId))
            .map(child -> new TaskDto(child.safeId(),
                child.getName(),
                child.getDescription(),
                child.getStatus(),
                child.getParentId(),
                child.getList().safeId(),
                getFullTaskTree(child)))
            .toList();
    }

    private void checkTaskListAndParent(long listId, @NotNull Task parent) {
        if (parent.getList().safeId() != listId) {
            throw new IllegalArgumentException(String.format("Parent list id (%d) does not match child list id (%d)",
                parent.safeId(), listId));
        }
    }

    private void checkGroupMemberPermission(KikaPrincipal principal, Group group) {
        if (group.getMembers().stream()
            .noneMatch(member -> member.getAccount().safeId() == principal.accountId()
                && member.getRole() != AccountRole.Role.RESTRICTED)) {
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

    private void propagateNotCompletedStatus(Task task) {
        if (task.getParent() != null) {
            task.getParent().setStatus(Task.Status.NOT_COMPLETED);
            propagateNotCompletedStatus(task.getParent());
        }
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
            getFullTaskTree(task));
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
        if (listId != null) {
            if (task.getList().safeId() != listId) {
                TaskList list = taskListRepository.getById(listId);
                task.setParent(null);
                task.setList(list);
                task.moveChildrenIntoList(list);
            }
        }
        if(!Objects.equals(parentId, task.getParentId())) {
            if (parentId == null) {
                task.setParent(null);
            } else {
                Task parent = taskRepository.getById(parentId);
                if (listId != null) {
                    checkTaskListAndParent(listId, parent);
                }
                task.setParent(parent);
                if (parent.getList().safeId() != task.getList().safeId()) {
                    task.setList(parent.getList());
                }
            }
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
        if (status == Task.Status.COMPLETED
            && task.getChildren().stream().anyMatch(child -> child.getStatus() == Task.Status.NOT_COMPLETED)) {
            throw new IllegalArgumentException(
                "Task (id=%d) can't be completed: there are incomplete child tasks".formatted(id));
        }
        checkListAccess(principal, task.getList());
        task.setStatus(status);
        if (status == Task.Status.NOT_COMPLETED) {
            propagateNotCompletedStatus(task);
        }
    }

    @Transactional
    public void edit(long id, EditTaskRequest request, KikaPrincipal principal) {
        Task task = taskRepository.getById(id);
        if (!Objects.equals(request.name(), task.getName())) {
            rename(id, request.name(), principal);
        }
        if (!Objects.equals(task.getDescription(), request.description())) {
            setDescription(id, request.description(), principal);
        }

        move(id, request.listId(), request.parentId(), principal);
    }

    @Transactional
    public GetTaskInfoResponse getInfo(long id, KikaPrincipal principal) {
        Task task = taskRepository.getById(id);
        runAccessChecks(principal, task.getList());
        return new GetTaskInfoResponse(id,
            task.getName(),
            task.getDescription(),
            task.isSubscribed(principal.accountId()),
            task.isAssigned(principal.accountId()),
            task.getParent() != null ?
                new GetTaskInfoResponse.ParentTask(task.getParent().safeId(), task.getParent().getName()) : null,
            new GetTaskInfoResponse.SubTaskList(task.getList().safeId(), task.getList().getName()),
            new GetTaskInfoResponse.SubGroup(task.getList().getGroup().safeId(), task.getList().getGroup().getName(),
                task.getList().getGroup().getRole(principal.accountId())),
            new ArrayList<>(task.getChildren().stream()
                .map(child -> new GetTaskInfoResponse.ChildTask(child.safeId(), child.getName(), child.getStatus()))
                .toList()),
            task.getStatus());
    }

    @Transactional
    public GetTaskEditInfoResponse getEditInfo(long id, KikaPrincipal principal) {
        Task task = taskRepository.getById(id);
        runAccessChecks(principal, task.getList());
        return new GetTaskEditInfoResponse(task.safeId(), task.getName(), task.getDescription(),
            task.getParent() != null ?
                new GetTaskEditInfoResponse.ParentTask(task.getParent().safeId(), task.getParent().getName()) : null,
            new GetTaskEditInfoResponse.SubList(task.getList().safeId(), task.getList().getName(),
                task.getList().getGroup().safeId()));
    }
}
