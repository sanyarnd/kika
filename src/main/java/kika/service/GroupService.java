package kika.service;

import kika.domain.Account;
import kika.domain.AccountRole;
import kika.domain.Group;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountRoleRepository;
import kika.repository.GroupRepository;
import kika.repository.TaskListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final TaskListRepository taskListRepository;

    @Transactional
    public long create(String name, long ownerId) {
        Account owner = accountRepository.getById(ownerId);
        Group group = groupRepository.save(new Group(name, owner));
        AccountRole role = new AccountRole(group, owner);
        group.addMember(role);

        group = groupRepository.save(group);
        return group.safeId();
    }

    @Transactional
    public void rename(long id, String name) {
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
        if(!group.getMembers().stream()
                .map(accountRole -> accountRole.getAccount().safeId())
                .collect(Collectors.toSet())
                .contains(newOwnerId)) {
            throw new IllegalArgumentException();
        }
        return accountRoleRepository.findById(new AccountRole.PK(groupId, newOwnerId))
                .map(accountRole -> {
                    group.getMembers().stream()
                            .filter(accountRole1 -> accountRole1.getRole() == AccountRole.Role.OWNER)
                            .findFirst().orElseThrow().setRole(AccountRole.Role.MEMBER);
                    group.setOwner(accountRole.getAccount());
                    accountRole.setRole(AccountRole.Role.OWNER);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public void addMember(long groupId, long id, AccountRole.Role role) {
        if(role == AccountRole.Role.OWNER) {
            throw new IllegalArgumentException();
        }
        Group group = groupRepository.getById(groupId);
        Account member = accountRepository.getById(id);
        AccountRole existingMember = group.getMembers().stream()
                .filter(accountRole -> accountRole.getAccount().safeId() == member.safeId())
                .findFirst().orElse(null);
        if(existingMember == null) {
            group.addMember(new AccountRole(group, member, role));
        } else {
            existingMember.setRole(role);
        }
    }

    @Transactional
    public boolean removeMember(long groupId, long memberId) {
        Group group = groupRepository.getById(groupId);
        if (group.getOwner().getId() == memberId) {
            throw new IllegalArgumentException();
        }
        if(group.getMembers().removeIf(accountRole -> accountRole.getAccount().safeId() == memberId)) {
            group.getTaskLists().forEach(list -> list.revokeSpecialAccessRecursively(memberId));
            return true;
        }
        return false;
    }

    @Transactional
    public void changeMemberRole(long groupId, long memberId, AccountRole.Role role) {
        Group group = groupRepository.getById(groupId);
        if (group.getOwner().safeId() == memberId || role == AccountRole.Role.OWNER) {
            throw new IllegalArgumentException();
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
