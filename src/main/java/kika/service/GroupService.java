package kika.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import kika.controller.request.AddGroupMemberRequest;
import kika.controller.response.GetTaskListResponse;
import kika.domain.Account;
import kika.domain.AccountRole;
import kika.domain.Group;
import kika.domain.Task;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountRoleRepository;
import kika.repository.GroupRepository;
import kika.repository.TaskListRepository;
import kika.security.principal.KikaPrincipal;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final TaskListRepository taskListRepository;

    private void checkOwnerAccess(KikaPrincipal principal, Group group) {
        if (principal.accountId() != group.getOwner().safeId()) {
            throw new BadCredentialsException("Owner access denied");
        }
    }

    private void checkMemberAccess(KikaPrincipal principal, Group group) {
        if (group.getMembers().stream()
            .map(accountRole -> accountRole.getAccount().safeId())
            .noneMatch(memberId -> memberId == principal.accountId())) {
            throw new BadCredentialsException("Access denied");
        }
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
    public long create(@NotNull String name, KikaPrincipal principal) {
        Account owner = accountRepository.getById(principal.accountId());
        Group group = groupRepository.save(new Group(name, owner));
        AccountRole ownerRole = new AccountRole(group, owner);
        group.addMember(ownerRole);

        return group.safeId();
    }

    @Transactional
    public void rename(long id, @NotNull String name, KikaPrincipal principal) {
        Group group = groupRepository.getById(id);
        checkOwnerAccess(principal, group);
        group.setName(name);
    }

    @Transactional
    public Group get(long id, KikaPrincipal principal) {
        Group group = groupRepository.getById(id);
        checkMemberAccess(principal, group);
        return group;
    }

    @Transactional
    public void delete(long id, KikaPrincipal principal) {
        Group group = groupRepository.getById(id);
        checkOwnerAccess(principal, group);
        groupRepository.delete(group);
    }

    @Transactional
    public boolean transferOwnership(long groupId, long newOwnerId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkOwnerAccess(principal, group);
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
    public void addMember(long groupId, long newMemberId, @NotNull AccountRole.Role role, KikaPrincipal principal) {
        if (role == AccountRole.Role.OWNER) {
            throw new IllegalArgumentException("Can't add a new member with role \"" + role + "\"");
        }
        Group group = groupRepository.getById(groupId);
        checkOwnerAccess(principal, group);
        Account member = accountRepository.getById(newMemberId);
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
    public void removeMember(long groupId, long memberId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkOwnerAccess(principal, group);
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
            .filter(list -> list.getSpecialAccess().stream()
                .anyMatch(accSpAcc -> accSpAcc.getAccount().safeId() == memberId))
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
    public void changeMemberRole(long groupId, long memberId, @NotNull AccountRole.Role role, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkOwnerAccess(principal, group);
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
    public Collection<AccountRole> getMembers(long groupId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkMemberAccess(principal, group);
        return new HashSet<>(group.getMembers());
    }

    @Transactional
    public List<GetTaskListResponse> getTaskLists(long groupId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkMemberAccess(principal, group);
        return group.getTaskLists().stream()
            .filter(list -> list.accountHasAccess(principal.accountId()) && list.getParent() == null)
            .map(list -> new GetTaskListResponse(list.safeId(), list.getName(),
                list.getParentId(),
                list.getGroup().safeId(),
                getFullChildrenTree(list, principal.accountId()),
                list.getTasks().stream()
                    .filter(task -> task.getParent() == null)
                    .map(task -> new TaskDto(task.safeId(),
                        task.getName(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getParentId(),
                        task.getList().safeId(),
                        getFullTaskTree(task)))
                    .toList()))
            .toList();
    }

    @Transactional
    public void edit(long groupId, String name, AddGroupMemberRequest[] members, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        if (!Objects.equals(name, group.getName())) {
            group.setName(name);
        }
        if (members != null) {
            group.getMembers().removeAll(group.getMembers().stream()
                .filter(m -> m.getRole() != AccountRole.Role.OWNER)
                .collect(Collectors.toSet()));
            for (AddGroupMemberRequest member : members) {
                addMember(groupId, member.getId(), member.getRole(), principal);
            }
        }
    }
}
