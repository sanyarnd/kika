package kika.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import kika.controller.request.AddGroupMemberRequest;
import kika.controller.response.AccountWithAccess;
import kika.controller.response.GetGroupEditInfoListsResponse;
import kika.controller.response.GetGroupEditInfoResponse;
import kika.controller.response.GetGroupInfoResponse;
import kika.controller.response.GetGroupTreeResponse;
import kika.controller.response.GetTaskListResponse;
import kika.controller.response.GroupMemberResponse;
import kika.controller.response.MessageBulk;
import kika.controller.response.TaskListSpecialAccessResponse;
import kika.domain.Account;
import kika.domain.AccountRole;
import kika.domain.AutoPersistable;
import kika.domain.Group;
import kika.domain.Task;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountRoleRepository;
import kika.repository.GroupRepository;
import kika.repository.TaskListRepository;
import kika.repository.TaskRepository;
import kika.security.principal.KikaPrincipal;
import kika.service.dto.ConciseTaskDto;
import kika.service.dto.ConciseTaskListDto;
import kika.service.dto.GroupDto;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private final TaskRepository taskRepository;

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

    private List<GetTaskListResponse> getFullChildrenTree(TaskList list, long accId) {
        if (list.getChildren().isEmpty()) {
            return List.of();
        }
        return list.getChildren().stream().filter(child -> child.accountHasAccess(accId))
            .sorted(Comparator.comparingLong(AutoPersistable::safeId))
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

    private List<ConciseTaskDto> getFullTaskTreeConcise(Task task) {
        if (task.getChildren().isEmpty()) {
            return List.of();
        }
        return task.getChildren().stream()
            .sorted(Comparator.comparingLong(AutoPersistable::safeId))
            .map(child -> new ConciseTaskDto(child.safeId(),
                child.getName(),
                getFullTaskTreeConcise(child)))
            .toList();
    }

    private List<ConciseTaskListDto> getFullChildrenTreeConcise(TaskList list, long accId) {
        if (list.getChildren().isEmpty()) {
            return List.of();
        }
        return list.getChildren().stream().filter(child -> child.accountHasAccess(accId))
            .sorted(Comparator.comparingLong(AutoPersistable::safeId))
            .map(child -> new ConciseTaskListDto(child.safeId(),
                child.getName(),
                getFullChildrenTreeConcise(child, accId),
                child.rootTasks().stream()
                    .map(task -> new ConciseTaskDto(task.safeId(), task.getName(), getFullTaskTreeConcise(task)))
                    .toList()))
            .toList();
    }

    @Transactional
    public long create(@NotNull String name, @Nullable List<AddGroupMemberRequest> members, KikaPrincipal principal) {
        Account owner = accountRepository.getById(principal.accountId());
        Group group = groupRepository.save(new Group(name, owner));
        AccountRole ownerRole = new AccountRole(group, owner);
        group.addMember(ownerRole);

        if (members != null) {
            members.forEach(newMember -> {
                AccountRole member =
                    new AccountRole(group, accountRepository.getById(newMember.getId()), newMember.getRole());
                group.addMember(member);
            });
        }

        return group.safeId();
    }

    @Transactional
    public void rename(long id, @NotNull String name, KikaPrincipal principal) {
        Group group = groupRepository.getById(id);
        checkOwnerAccess(principal, group);
        group.setName(name);
    }

    @Transactional
    public GroupDto get(long id, KikaPrincipal principal) {
        Group group = groupRepository.getById(id);
        checkMemberAccess(principal, group);
        return new GroupDto(group.safeId(), group.getName(), group.getRole(principal.accountId()),
            group.getOwner().safeId(), group.getOwner().getName(), group.getMessages().size());
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
        if (group.getMember(newMemberId) == null) {
            group.addMember(new AccountRole(group, accountRepository.getById(newMemberId), role));
        } else {
            group.getMember(newMemberId).setRole(role);
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
        return group.rootLists().stream()
            .filter(list -> list.accountHasAccess(principal.accountId()))
            .sorted(Comparator.comparingLong(AutoPersistable::safeId))
            .map(list -> new GetTaskListResponse(list.safeId(), list.getName(),
                list.getParentId(),
                list.getGroup().safeId(),
                getFullChildrenTree(list, principal.accountId()),
                list.rootTasks().stream()
                    .sorted(Comparator.comparingLong(AutoPersistable::safeId))
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
            Set<Long> ids = Arrays.stream(members).map(AddGroupMemberRequest::getId).collect(Collectors.toSet());
            group.getMembers().stream()
                .filter(accountRole -> !ids.contains(accountRole.getId().getAccountId()) &&
                    (accountRole.getId().getAccountId() != group.getOwner().safeId()))
                .forEach(group::removeMember);
            for (AddGroupMemberRequest member : members) {
                addMember(groupId, member.getId(), member.getRole(), principal);
            }
        }
    }

    @Transactional
    public GetGroupInfoResponse getInfo(long id, long offset, long count, KikaPrincipal principal) {
        Group group = groupRepository.getById(id);
        checkMemberAccess(principal, group);
        return new GetGroupInfoResponse(id, group.getName(), group.getRole(principal.accountId()),
            group.rootLists().stream()
                .filter(taskList -> taskList.accountHasAccess(principal.accountId()))
                .map(taskList -> new GetGroupInfoResponse.SubTaskList(taskList.safeId(), taskList.getName()))
                .toList(),
            new MessageBulk(group.getMessages().stream()
                .sorted((m1, m2) -> Long.compare(m2.safeId(), m1.safeId()))
                .skip(offset)
                .limit(count)
                .map(
                    message -> new MessageBulk.SubMessage(message.safeId(), message.getCreatedDate(), message.getBody(),
                        accountRepository.findById(Long.parseLong(message.getCreatedBy())).map(Account::getName)
                            .orElse("(аккаунт удален)")))
                .toList(),
                group.getMessages().size(),
                0));
    }

    @Transactional
    public void deleteByGroup(long groupId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkOwnerAccess(principal, group);
        group.getMessages().clear();
    }

    @Transactional
    public GetGroupEditInfoResponse getEditInfo(long id, KikaPrincipal principal) {
        Group group = groupRepository.getById(id);
        checkOwnerAccess(principal, group);
        return new GetGroupEditInfoResponse(group.safeId(), group.getName(),
            group.getMembers().stream()
                .map(accountRole -> new GroupMemberResponse(accountRole.getId().getAccountId(), accountRole.getRole(),
                    accountRole.getAccount().getName()))
                .toList(),
            group.getMessages().size());
    }

    private List<GetGroupTreeResponse.SubTaskList> subTaskListTree(
        TaskList parent,
        Long skipList,
        Long skipTask,
        boolean keepTasks
    ) {
        if (parent.getChildren().isEmpty()) {
            return List.of();
        }
        return parent.getChildren().stream()
            .filter(taskList -> !Objects.equals(skipList, taskList.safeId()))
            .map(taskList -> new GetGroupTreeResponse.SubTaskList(taskList.safeId(), taskList.getName(),
                subTaskListTree(taskList, skipList, skipTask, keepTasks),
                keepTasks ? taskList.rootTasks().stream()
                    .map(task -> new GetGroupTreeResponse.SubTask(task.safeId(), task.getName(),
                        subTaskTree(task, skipTask)))
                    .toList() : List.of()))
            .toList();
    }

    private List<GetGroupTreeResponse.SubTask> subTaskTree(Task parent, Long skipTask) {
        if (parent.getChildren().isEmpty()) {
            return List.of();
        }
        return parent.getChildren().stream()
            .filter(task -> !Objects.equals(task.safeId(), skipTask))
            .map(task -> new GetGroupTreeResponse.SubTask(task.safeId(), task.getName(), subTaskTree(task, skipTask)))
            .toList();
    }

    @Transactional
    public GetGroupTreeResponse getTree(long groupId, long listId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkMemberAccess(principal, group);
        if (taskListRepository.getById(listId).getGroup().safeId() != groupId) {
            throw new IllegalArgumentException("Group (id=%d) has no list (id=%d)".formatted(groupId, listId));
        }

        return new GetGroupTreeResponse(group.safeId(), group.getName(),
            group.rootLists().stream()
                .filter(taskList -> taskList.safeId() != listId)
                .map(taskList -> new GetGroupTreeResponse.SubTaskList(taskList.safeId(), taskList.getName(),
                    subTaskListTree(taskList, listId, null, false),
                    List.of()))
                .toList());
    }

    @Transactional
    public GetGroupTreeResponse getTreeWithTasks(long groupId, long taskId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkMemberAccess(principal, group);
        if (taskRepository.getById(taskId).getList().getGroup().safeId() != groupId) {
            throw new IllegalArgumentException("Group (id=%d) has no task (id=%d)".formatted(groupId, taskId));
        }

        return new GetGroupTreeResponse(group.safeId(), group.getName(),
            group.rootLists().stream()
                .map(taskList -> new GetGroupTreeResponse.SubTaskList(taskList.safeId(), taskList.getName(),
                    subTaskListTree(taskList, null, taskId, true),
                    List.of()))
                .toList());
    }

    @Transactional
    public GetGroupEditInfoListsResponse getEditInfoLists(long id, KikaPrincipal principal) {
        Group group = groupRepository.getById(id);
        checkOwnerAccess(principal, group);

        return new GetGroupEditInfoListsResponse(group.safeId(),
            group.getName(),
            group.rootLists().stream()
                .filter(list -> list.accountHasAccess(principal.accountId()))
                .sorted(Comparator.comparingLong(AutoPersistable::safeId))
                .map(list -> new ConciseTaskListDto(list.safeId(), list.getName(),
                    getFullChildrenTreeConcise(list, principal.accountId()), list.rootTasks().stream()
                    .map(task -> new ConciseTaskDto(task.safeId(), task.getName(), getFullTaskTreeConcise(task)))
                    .toList()))
                .toList(),
            new TaskListSpecialAccessResponse(false, group.getMembers().stream()
                .map(accountRole -> new AccountWithAccess(accountRole.getId().getAccountId(),
                    accountRole.getAccount().getName(),
                    Boolean.TRUE))
                .toList()));
    }
}
