package kika.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kika.domain.Account;
import kika.domain.AccountRole;
import kika.domain.AccountSpecialAccess;
import kika.domain.AccountTaskAssignee;
import kika.domain.AccountTaskSubscriber;
import kika.domain.AutoPersistable;
import kika.domain.Task;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountSpecialAccessRepository;
import kika.repository.GroupRepository;
import kika.repository.TaskListRepository;
import kika.security.principal.KikaPrincipal;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final GroupRepository groupRepository;
    private final TaskListRepository taskListRepository;
    private final AccountSpecialAccessRepository accountSpecialAccessRepository;

    private final TaskListService taskListService;

    private void checkAccess(KikaPrincipal principal, long accountId) {
        if(principal.accountId() != accountId) {
            throw new BadCredentialsException("Access denied");
        }
    }

//    public long register(@NotNull String name) {
//        return accountRepository.save(new Account(name)).safeId();
//    }

    public Collection<AccountRole> getGroups(long accountId, KikaPrincipal principal) {
        checkAccess(principal, accountId);
        return groupRepository.getAccountGroups(accountId);
    }

    @Transactional
    public List<TaskList> getTaskLists(long accountId, long groupId, KikaPrincipal principal) {
        checkAccess(principal, accountId);
        List<TaskList> lists = taskListRepository.getTaskListsByGroupId(groupId);

        return lists.stream()
            .filter(list -> list.accountHasAccess(accountId))
            .collect(Collectors.toList());
    }

    @Transactional
    public void rename(long accountId, @NotNull String name, KikaPrincipal principal) {
        checkAccess(principal, accountId);
        accountRepository.getById(accountId).setName(name);
    }

    @Transactional
    public boolean delete(long accountId, KikaPrincipal principal) {
        checkAccess(principal, accountId);
        List<AccountSpecialAccess> listsWithOneSpecialAccessAccountById =
            accountSpecialAccessRepository.getListsWithOneSpecialAccessAccountById(accountId);
        Account account = accountRepository.getById(accountId);

        account.getAccountRoles().clear();

        if (!listsWithOneSpecialAccessAccountById.isEmpty()) {
            taskListRepository.deleteAll(listsWithOneSpecialAccessAccountById.stream()
                .map(AccountSpecialAccess::getTaskList)
                .collect(Collectors.toList()));
        }
        accountRepository.delete(account);
        return true;
    }

    public Account get(long accountId, KikaPrincipal principal) {
        checkAccess(principal, accountId);
        return accountRepository.findById(accountId).orElse(null);
    }

    @Transactional
    public Set<Task> assignedTasks(long accountId, KikaPrincipal principal) {
        checkAccess(principal, accountId);
        return accountRepository.getById(accountId).getAssignedTasks().stream()
            .map(AccountTaskAssignee::getTask)
            .collect(Collectors.toSet());
    }

    @Transactional
    public Set<Task> subscribedTasks(long accountId, KikaPrincipal principal) {
        checkAccess(principal, accountId);
        return accountRepository.getById(accountId).getSubscribedTasks().stream()
            .map(AccountTaskSubscriber::getTask)
            .collect(Collectors.toSet());
    }
}
