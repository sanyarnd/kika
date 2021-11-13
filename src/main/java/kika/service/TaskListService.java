package kika.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import kika.controller.request.EditTaskListRequest;
import kika.controller.response.AccountWithAccess;
import kika.controller.response.GetTaskListResponse;
import kika.controller.response.TaskListSpecialAccessResponse;
import kika.domain.Account;
import kika.domain.AccountRole;
import kika.domain.AccountSpecialAccess;
import kika.domain.AutoPersistable;
import kika.domain.Group;
import kika.domain.Task;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountSpecialAccessRepository;
import kika.repository.GroupRepository;
import kika.repository.TaskListRepository;
import kika.repository.TaskRepository;
import kika.security.principal.KikaPrincipal;
import kika.service.dto.TaskDto;
import kika.service.dto.TaskListDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskListService {
    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final AccountRepository accountRepository;
    private final AccountSpecialAccessRepository accountSpecialAccessRepository;

    private void revokeSpecialAccessRecursively(
        Set<AccountSpecialAccess> specialAccess,
        long id,
        Set<TaskList> children
    ) {
        if (specialAccess.removeIf(accSpAcc -> accSpAcc.getAccount().safeId() == id)) {
            children.forEach(child -> revokeSpecialAccessRecursively(child.getSpecialAccess(), id, children));
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
        Set<AccountSpecialAccess> specialAccess = list.getSpecialAccess();
        if (!specialAccess.isEmpty() && specialAccess.stream()
            .map(accountSpecialAccess -> accountSpecialAccess.getAccount().safeId())
            .noneMatch(memberId -> memberId == principal.accountId())) {
            throw new BadCredentialsException("No access");
        }
    }

    private void runAccessChecks(KikaPrincipal principal, TaskList list) {
        checkGroupMemberPermission(principal, list.getGroup());
        checkListAccess(principal, list);
    }

    private List<TaskDto> getFullTaskTree(Task task) {
        if (task.getChildren().isEmpty()) {
            return List.of();
        }
        return task.getChildren().stream()
            .map(child -> new TaskDto(child.safeId(),
                child.getName(),
                child.getDescription(),
                child.getStatus(),
                child.getParentId(),
                child.getList().safeId(),
                getFullTaskTree(child)))
            .toList();
    }

    private List<GetTaskListResponse> getFullChildrenTree(TaskList list, long accId) {
        if (list.getChildren().isEmpty()) {
            return List.of();
        }
        return list.getChildren().stream().filter(child -> child.accountHasAccess(accId))
            .map(child -> new GetTaskListResponse(child.safeId(),
                child.getName(),
                child.getParentId(),
                child.getGroup().safeId(),
                getFullChildrenTree(child, accId),
                child.getTasks().stream()
                    .map(task -> new TaskDto(task.safeId(),
                        task.getName(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getParentId(),
                        task.getList().safeId(),
                        getFullTaskTree(task))).toList()))
            .toList();
    }

    @Transactional
    public long create(String name, Long parentId, long groupId, Set<Long> accessList, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkGroupMemberPermission(principal, group);
        TaskList parent = parentId == null ? null : taskListRepository.getById(parentId);
        if (parent != null) {
            checkListAccess(principal, parent);
        }
        TaskList list = taskListRepository.saveAndFlush(new TaskList(name, parent, group));
        if (accessList != null) {
            setSpecialAccess(list.safeId(), accessList, principal);
        }
        return list.safeId();
    }

    @Transactional
    public void rename(long id, @NotNull String name, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        list.setName(name);
    }

    @Transactional
    public TaskListDto get(long id, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        return new TaskListDto(id, list.getName(), list.getParentId(), list.getGroup().safeId(),
            list.getChildren().stream().map(child -> new GetTaskListResponse(child.safeId(), child.getName(),
                child.getParentId(),
                child.getGroup().safeId(),
                getFullChildrenTree(child, principal.accountId()),
                child.getTasks().stream()
                    .filter(task -> task.getParent() == null)
                    .map(task -> new TaskDto(task.safeId(),
                        task.getName(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getParentId(),
                        task.getList().safeId(),
                        getFullTaskTree(task)))
                    .toList())).toList(),
            list.getTasks().stream()
                .filter(task -> task.getParent() == null)
                .map(task -> new TaskDto(task.safeId(),
                    task.getName(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getParentId(),
                    task.getList().safeId(),
                    getFullTaskTree(task)))
                .toList());
    }

    private Set<Account> getParentAccessAccounts(TaskList list) {
        if (list.getParent() == null) {
            return list.getGroup().getMembers().stream().map(AccountRole::getAccount).collect(Collectors.toSet());
        }

        if (list.getParent().getSpecialAccess().isEmpty()) {
            return getParentAccessAccounts(list.getParent());
        } else {
            return list.getParent().getSpecialAccess().stream().map(AccountSpecialAccess::getAccount).collect(
                Collectors.toSet());
        }

    }

    @Transactional
    public TaskListSpecialAccessResponse getAccountsWithAccess(long id, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        Set<Account> accountsWithSpecialAccess =
            accountSpecialAccessRepository.getSpecialAccessAccountsByTaskListId(id).stream()
                .map(AccountSpecialAccess::getAccount)
                .collect(Collectors.toSet());
        Set<Account> parentAccess = getParentAccessAccounts(list);
        if (!accountsWithSpecialAccess.isEmpty()) {
            // If there are access restrictions, return all accounts that have access plus all accounts
            // that have access to the parent element
            Set<Long> spAccIds = accountsWithSpecialAccess.stream()
                .map(AutoPersistable::safeId).collect(Collectors.toSet());
            return new TaskListSpecialAccessResponse(true, parentAccess.stream()
                .filter(account -> !spAccIds.contains(account.safeId()))
                .map(account -> new AccountWithAccess(account.safeId(), account.getName(), false))
                .collect(Collectors.toCollection(() -> accountsWithSpecialAccess.stream()
                    .map(acc -> new AccountWithAccess(acc.safeId(), acc.getName(), true))
                    .collect(Collectors.toList()))));
        } else {
            // If there are no access restrictions, return all accounts that have access to the parent element
            return new TaskListSpecialAccessResponse(false, parentAccess.stream()
                .map(account -> new AccountWithAccess(account.safeId(), account.getName(), true))
                .toList());
        }
    }

    @Transactional
    public void setSpecialAccess(long id, @NotNull Set<Long> membersWithAccessIds, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        TaskListSpecialAccessResponse accountsWithPotentialAccess = getAccountsWithAccess(id, principal);
        if (accountsWithPotentialAccess.isSet()) {
            Set<Long> accs = accountsWithPotentialAccess.getAccounts().stream()
                .filter(AccountWithAccess::isHasAccess)
                .map(AccountWithAccess::getId)
                .collect(Collectors.toSet());
            if (membersWithAccessIds.size() == accs.size() && accs.containsAll(membersWithAccessIds)) {
                return;
            }
        }
//        else if (membersWithAccessIds.size() == accountsWithPotentialAccess.getAccounts().size() &&
//            accountsWithPotentialAccess.getAccounts().stream().map(AccountWithAccess::getId).collect(Collectors.toSet())
//                .containsAll(membersWithAccessIds)) {
//            return;
//        }
        if (!membersWithAccessIds.isEmpty()) {
            if (!accountsWithPotentialAccess.getAccounts().stream()
                .map(AccountWithAccess::getId)
                .collect(Collectors.toSet())
                .containsAll(membersWithAccessIds)) {
                throw new IllegalArgumentException("The new access set must be a subset of the parent access set");
            }
            // Recursively revoke access from accounts from this list and all its children
            list.getSpecialAccess().stream()
                .filter(accSpAcc -> membersWithAccessIds.contains(accSpAcc.getAccount().safeId()))
                .forEach(accSpAcc -> revokeSpecialAccessRecursively(list.getSpecialAccess(),
                    accSpAcc.getAccount().safeId(), list.getChildren()));
            // Add access to accounts that do not have it yet
            Set<Long> currentAccessIds = list.getSpecialAccess().stream()
                .map(accSpAcc -> accSpAcc.getAccount().safeId())
                .collect(Collectors.toSet());
            membersWithAccessIds.stream()
                .filter(accId -> !currentAccessIds.contains(accId))
                .map(accountRepository::getById)
                .forEach(account -> accountSpecialAccessRepository.save(new AccountSpecialAccess(list, account)));

        } else {
            // If the new special access set is blank (= remove all access restrictions),
            // then clearing it won't affect child lists' access restrictions
            list.getSpecialAccess().clear();
        }
    }

    @Transactional
    public void delete(long id, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        taskListRepository.delete(list);
    }

    @Transactional
    public Set<TaskDto> getTasks(long id, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        return list.getTasks().stream()
            .filter(task -> task.getParent() == null)
            .map(task -> new TaskDto(task.safeId(), task.getName(), task.getDescription(), task.getStatus(),
                task.getParentId(), task.getList().safeId(),
                getFullTaskTree(task)))
            .collect(Collectors.toSet());
    }

    @Transactional
    public void edit(long id, EditTaskListRequest data, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        if (!Objects.equals(list.getName(), data.getName())) {
            rename(id, data.getName(), principal);
        }

//        TODO: implement list move

        setSpecialAccess(id, data.getAccessList(), principal);
    }
}
