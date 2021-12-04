package kika.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kika.domain.Account;
import kika.domain.AccountSpecialAccess;
import kika.domain.AccountTaskAssignee;
import kika.domain.AccountTaskSubscriber;
import kika.domain.Task;
import kika.domain.TaskList;
import kika.repository.AccountRepository;
import kika.repository.AccountSpecialAccessRepository;
import kika.repository.GroupRepository;
import kika.repository.TaskListRepository;
import kika.security.principal.KikaPrincipal;
import kika.service.dto.AccountDto;
import kika.service.dto.GroupDto;
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
        if (principal.accountId() != accountId) {
            throw new BadCredentialsException("Access denied");
        }
    }

//    public long register(@NotNull String name) {
//        return accountRepository.save(new Account(name)).safeId();
//    }

    @Transactional
    public List<GroupDto> getGroups(KikaPrincipal principal) {
        return groupRepository.getAccountGroups(principal.accountId()).stream()
            .map(accountRole -> new GroupDto(accountRole.getId().getGroupId(),
                accountRole.getGroup().getName(),
                accountRole.getRole(),
                accountRole.getGroup().getOwner().safeId(),
                accountRole.getGroup().getOwner().getName(),
                accountRole.getGroup().getMessages().size()))
            .toList();
    }

    @Transactional
    public List<TaskList> getTaskLists(long groupId, KikaPrincipal principal) {
        List<TaskList> lists = taskListRepository.getTaskListsByGroupId(groupId);

        return lists.stream()
            .filter(list -> list.accountHasAccess(principal.accountId()))
            .toList();
    }

    @Transactional
    public void rename(@NotNull String name, KikaPrincipal principal) {
        accountRepository.getById(principal.accountId()).setName(name);
    }

    @Transactional
    public boolean delete(KikaPrincipal principal) {
        List<AccountSpecialAccess> listsWithOneSpecialAccessAccountById =
            accountSpecialAccessRepository.getListsWithOneSpecialAccessAccountById(principal.accountId());
        Account account = accountRepository.getById(principal.accountId());

        account.getAccountRoles().clear();

        if (!listsWithOneSpecialAccessAccountById.isEmpty()) {
            taskListRepository.deleteAll(listsWithOneSpecialAccessAccountById.stream()
                .map(AccountSpecialAccess::getTaskList)
                .collect(Collectors.toList()));
        }
        accountRepository.delete(account);
        return true;
    }

    @Transactional
    public AccountDto get(KikaPrincipal principal) {
        Account account = accountRepository.getById(principal.accountId());
        return new AccountDto(account.safeId(), account.getName());
    }

    @Transactional
    public AccountDto getById(long id, KikaPrincipal principal) {
        Account account = accountRepository.getById(id);
        return new AccountDto(account.safeId(), account.getName());
    }

    @Transactional
    public Set<Task> assignedTasks(KikaPrincipal principal) {
        return accountRepository.getById(principal.accountId()).getAssignedTasks().stream()
            .map(AccountTaskAssignee::getTask)
            .collect(Collectors.toSet());
    }

    @Transactional
    public Set<Task> subscribedTasks(KikaPrincipal principal) {
        return accountRepository.getById(principal.accountId()).getSubscribedTasks().stream()
            .map(AccountTaskSubscriber::getTask)
            .collect(Collectors.toSet());
    }
}
