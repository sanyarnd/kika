package kika.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kika.domain.Account;
import kika.domain.AccountRole;
import kika.domain.Group;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountRoleRepository;
import kika.repository.AccountSpecialAccessRepository;
import kika.repository.GroupRepository;
import kika.repository.TaskListRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final TaskListRepository taskListRepository;
    private final AccountSpecialAccessRepository accountSpecialAccessRepository;

    @Transactional
    public long create(@NotNull String name, long ownerId) {
        Account owner = accountRepository.getById(ownerId);
        Group group = groupRepository.save(new Group(name, owner));
        AccountRole role = new AccountRole(group, owner);
        group.addMember(role);

        return group.safeId();
    }

    @Transactional
    public void rename(long id, @NotNull String name) {
        groupRepository.getById(id).setName(name);
    }

    @Transactional
    public Group get(long id) {
        return groupRepository.findById(id).orElse(null);
    }

    @Transactional
    public void delete(long id) {
        groupRepository.deleteById(id);
    }

    @Transactional
    public Collection<TaskList> getTaskLists(long id) {
        return taskListRepository.getTaskListsByGroupId(id);
    }

    @Transactional
    public boolean transferOwnership(long groupId, long newOwnerId) {
        Group group = groupRepository.getById(groupId);
        if (group.getMembers().stream().noneMatch(accountRole -> accountRole.getAccount().safeId() == newOwnerId)) {
            throw new IllegalArgumentException("New owner must be a member of the group");
        }
        return accountRoleRepository.findById(new AccountRole.PK(groupId, newOwnerId))
            .map(accountRole -> {
                group.getMembers().stream()
                    .filter(accountRole1 -> accountRole1.getRole() == AccountRole.Role.OWNER)
                    .findFirst().orElseThrow().setRole(AccountRole.Role.MEMBER);
                group.setOwner(accountRole.getAccount());
                accountRole.setRole(AccountRole.Role.OWNER);
                return Boolean.TRUE;
            }).orElse(Boolean.FALSE);
    }

    @Transactional
    public void addMember(long groupId, long id, @NotNull AccountRole.Role role) {
        if (role == AccountRole.Role.OWNER) {
            throw new IllegalArgumentException("Can't add a new member with role \"" + role + "\"");
        }
        Group group = groupRepository.getById(groupId);
        Account member = accountRepository.getById(id);
        AccountRole existingMember = group.getMembers().stream()
            .filter(accountRole -> accountRole.getAccount().safeId() == member.safeId())
            .findFirst().orElse(null);
        if (existingMember == null) {
            group.addMember(new AccountRole(group, member, role));
        } else {
            existingMember.setRole(role);
        }
    }

    @Transactional
    public void removeMember(long groupId, long memberId) {
        Group group = groupRepository.getById(groupId);
        if (group.getOwner().safeId() == memberId) {
            throw new IllegalArgumentException("Can't remove owner");
        }

        // Check if user is a member,
        // remove user from a group
        // delete lists that only member can access, remove member from other lists in the group

        Collection<AccountRole> accountRoles = groupRepository.getAccountGroups(memberId);
        Set<AccountRole> groupRoles = new HashSet<>(group.getMembers());
        groupRoles.retainAll(accountRoles);
        if (groupRoles.isEmpty()) {
            throw new IllegalArgumentException("Account is not a member of the group");
        }
        group.getMembers().removeAll(groupRoles);

        Collection<TaskList> listsByGroupId = taskListRepository.getTaskListsByGroupId(groupId);
        List<TaskList> taskLists = listsByGroupId.stream()
            .filter(list -> list.getSpecialAccess().stream().anyMatch(accSpAcc -> accSpAcc.getAccount().safeId() == memberId))
            .collect(Collectors.toList());

        for (TaskList list : taskLists) {
            if (list.getSpecialAccess().size() == 1) {
                taskListRepository.delete(list);
            } else {
                list.getSpecialAccess().removeIf(asa -> asa.getAccount().safeId() == memberId);
            }
        }

    }

    @Transactional
    public void changeMemberRole(long groupId, long memberId, @NotNull AccountRole.Role role) {
        Group group = groupRepository.getById(groupId);
        if (group.getOwner().safeId() == memberId) {
            throw new IllegalArgumentException("Can't change group owner's role");
        }
        if (role == AccountRole.Role.OWNER) {
            throw new IllegalArgumentException("Can't set account role to " + role);
        }
        group.getMembers()
            .stream()
            .filter(ar -> ar.getAccount().safeId() == memberId)
            .findFirst()
            .orElseThrow()
            .setRole(role);
    }

    @Transactional
    public Collection<AccountRole> getMembers(long groupId) {
        return new HashSet<>(groupRepository.getById(groupId).getMembers());
    }
}
