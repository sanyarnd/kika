package kika.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import kika.controller.request.RegisterAccountRequest;
import kika.controller.request.SetSingleNonNullablePropertyRequest;
import kika.controller.response.AccountGroupResponse;
import kika.controller.response.AccountGroupsResponse;
import kika.controller.response.AccountTaskListResponse;
import kika.controller.response.AccountTaskListsResponse;
import kika.controller.response.GetAccountResponse;
import kika.controller.response.GetAssignedTaskResponse;
import kika.controller.response.GetAssignedTasksResponse;
import kika.controller.response.GetSubscribedTaskResponse;
import kika.controller.response.GetSubscribedTasksResponse;
import kika.domain.Account;
import kika.domain.Task;
import kika.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Validated
public class AccountController {
    private final AccountService service;

    @PostMapping("/account/register")
    @ResponseBody
    public Long registerAccount(@Valid @RequestBody RegisterAccountRequest request) {
        return service.register(request.getName());
    }

    @PostMapping("/account/{id}/rename")
    public void renameAccount(@PathVariable long id, @RequestBody SetSingleNonNullablePropertyRequest request) {
        service.rename(id, request.getValue());
    }

    @DeleteMapping(value = "/account/{id}")
    public void deleteAccount(@PathVariable("id") long id) {
        service.delete(id);
    }

    @GetMapping("/account/{id}")
    public GetAccountResponse getAccount(@PathVariable("id") long id) {
        Account account = service.get(id);
        return new GetAccountResponse(account.getName());
    }

    @GetMapping("/account/{id}/groups")
    public AccountGroupsResponse getAccountGroups(@PathVariable long id) {
        List<AccountGroupResponse> accountGroupList = service.getGroups(id).stream()
            .map(accountRole -> new AccountGroupResponse(accountRole.getGroup().safeId(),
                accountRole.getGroup().getName(), accountRole.getRole()))
            .collect(Collectors.toList());
        return new AccountGroupsResponse(accountGroupList, accountGroupList.size());
    }

    @GetMapping("/account/{id}/lists")
    public AccountTaskListsResponse getAccountTaskLists(@PathVariable long id) {
        List<AccountTaskListResponse> taskLists = service.getTaskLists(id).stream()
            .map(list -> new AccountTaskListResponse(list.safeId(), list.getName(), list.getGroup().getId()))
            .collect(Collectors.toList());
        return new AccountTaskListsResponse(taskLists, taskLists.size());
    }

    @GetMapping("/account/{id}/tasks/assigned")
    public GetAssignedTasksResponse getAccountAssignedTasks(@PathVariable long id) {
        Set<Task> tasks = service.assignedTasks(id);
        return new GetAssignedTasksResponse(tasks.stream()
            .map(task -> new GetAssignedTaskResponse(task.safeId(), task.getName(), task.getStatus()))
            .collect(Collectors.toSet()), tasks.size());
    }

    @GetMapping("/account/{id}/tasks/subscribed")
    public GetSubscribedTasksResponse getAccountSubscribedTasks(@PathVariable long id) {
        Set<Task> tasks = service.subscribedTasks(id);
        return new GetSubscribedTasksResponse(tasks.stream()
            .map(task -> new GetSubscribedTaskResponse(task.safeId(), task.getName(), task.getStatus()))
            .collect(Collectors.toSet()), tasks.size());
    }
}
