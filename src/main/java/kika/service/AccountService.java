package kika.service;

import kika.domain.*;
import kika.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final GroupRepository groupRepository;
    private final TaskListRepository taskListRepository;
    private final AccountSpecialAccessRepository accountSpecialAccessRepository;

    private final TaskListService taskListService;

    public long register(String name) {
        return accountRepository.save(new Account(name)).safeId();
    }

    public Collection<AccountRole> getGroups(long id) {
        return groupRepository.getAccountGroups(id);
    }

    @Transactional
    public List<TaskList> getTaskLists(long id) {
        Set<Long> accountGroupIds = groupRepository.getAccountGroups(id).stream()
                .map(accountRole -> accountRole.getGroup().safeId())
                .collect(Collectors.toUnmodifiableSet());
        return accountGroupIds.stream()
                .flatMap(groupId -> taskListRepository.getTaskListsByGroupId(groupId).stream())
                .filter(list -> taskListService.getAccountsWithAccess(list.safeId())
                        .stream()
                        .map(AutoPersistable::safeId)
                        .collect(Collectors.toUnmodifiableSet()).contains(id))
                .collect(Collectors.toList());
    }

    @Transactional
    public void rename(long id, String name) {
        accountRepository.getById(id).setName(name);
    }

    @Transactional
    public boolean delete(long accountId) {
        List<AccountSpecialAccess> listsWithOneSpecialAccessAccountById =
                accountSpecialAccessRepository.getListsWithOneSpecialAccessAccountById(accountId);
        Account account = accountRepository.getById(accountId);

        account.getAccountRoles().clear();

        if(!listsWithOneSpecialAccessAccountById.isEmpty()) {
            taskListRepository.deleteAll(listsWithOneSpecialAccessAccountById.stream()
                    .map(AccountSpecialAccess::getTaskList)
                    .collect(Collectors.toList()));
        }
        accountRepository.delete(account);
        return true;
    }

    public Account get(long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    @Transactional
    public Set<Task> assignedTasks(long id) {
        return accountRepository.getById(id).getAssignedTasks().stream()
                .map(AccountTaskAssignee::getTask)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Set<Task> subscribedTasks(long id) {
        return accountRepository.getById(id).getSubscribedTasks().stream()
                .map(AccountTaskSubscriber::getTask)
                .collect(Collectors.toSet());
    }
}
