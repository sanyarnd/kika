package kika.service;

import kika.domain.*;
import kika.repository.*;
import kika.security.principal.KikaPrincipal;
import kika.service.dto.TaskDto;
import kika.service.dto.TaskListDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.HashSet;
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
        if (!list.getSpecialAccess().isEmpty() && list.getSpecialAccess().stream()
            .map(accountSpecialAccess -> accountSpecialAccess.getAccount().safeId())
            .noneMatch(memberId -> memberId == principal.accountId())) {
            throw new BadCredentialsException("No access");
        }
    }

    private void runAccessChecks(KikaPrincipal principal, TaskList list) {
        checkGroupMemberPermission(principal, list.getGroup());
        checkListAccess(principal, list);
    }

    @Transactional
    public long create(String name, Long parentId, long groupId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkGroupMemberPermission(principal, group);
        TaskList parent = parentId == null ? null : taskListRepository.getById(parentId);
        if (parent != null) {
            checkListAccess(principal, parent);
        }
        return taskListRepository.save(new TaskList(name, parent, group)).safeId();
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
            list.getChildren().stream().map(AutoPersistable::safeId).collect(Collectors.toSet()),
            list.getTasks().stream().map(AutoPersistable::safeId).collect(Collectors.toSet()));
    }

    @Transactional
    public Collection<Account> getAccountsWithAccess(long id, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        Set<Account> accountsWithSpecialAccess =
            accountSpecialAccessRepository.getSpecialAccessAccountsByTaskListId(id).stream()
                .map(AccountSpecialAccess::getAccount)
                .collect(Collectors.toSet());
        if (!accountsWithSpecialAccess.isEmpty()) {
            return accountsWithSpecialAccess;
        } else {
            if (list.getParent() != null) {
                accountsWithSpecialAccess = new HashSet<>(getAccountsWithAccess(list.getParentId(), principal));
                if (!accountsWithSpecialAccess.isEmpty()) {
                    return accountsWithSpecialAccess;
                }
            }
            return taskListRepository.getById(id).getGroup().getMembers().stream()
                .map(AccountRole::getAccount)
                .collect(Collectors.toList());
        }
    }

    @Transactional
    public void setSpecialAccess(long id, @NotNull Set<Long> membersWithAccessIds, KikaPrincipal principal) {
        TaskList list = taskListRepository.getById(id);
        runAccessChecks(principal, list);
        Set<Account> accountsWithPotentialAccess = new HashSet<>(getAccountsWithAccess(id, principal));
        if (membersWithAccessIds.size() == accountsWithPotentialAccess.size() &&
            accountsWithPotentialAccess.stream().map(Account::safeId).collect(Collectors.toSet())
                .containsAll(membersWithAccessIds)) {
            return;
        }
        if (!membersWithAccessIds.isEmpty()) {
            if (!accountsWithPotentialAccess.stream()
                .map(Account::safeId)
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
            .map(task -> new TaskDto(task.safeId(), task.getName(), task.getDescription(), task.getStatus(),
                task.getParentId(), task.getList().safeId(),
                task.getChildren().stream().map(AutoPersistable::safeId).collect(Collectors.toSet())))
            .collect(Collectors.toSet());
    }
}
