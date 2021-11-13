package kika.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kika.controller.request.SingleNonNullablePropertyRequest;
import kika.controller.response.AccountGroupsResponse;
import kika.controller.response.AccountTaskListResponse;
import kika.controller.response.AccountTaskListsResponse;
import kika.controller.response.GetAccountResponse;
import kika.controller.response.GetAssignedTaskResponse;
import kika.controller.response.GetAssignedTasksResponse;
import kika.controller.response.GetSubscribedTaskResponse;
import kika.controller.response.GetSubscribedTasksResponse;
import kika.domain.Task;
import kika.security.principal.KikaPrincipal;
import kika.service.AccountService;
import kika.service.dto.AccountDto;
import kika.service.dto.GroupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Validated
public class AccountController {
    private final AccountService service;

//    @PostMapping("/account/register")
//    @ResponseBody
//    public Long registerAccount(@Valid @RequestBody RegisterAccountRequest request) {
//        return service.register(request.getName());
//    }

    @PostMapping("/account")
    public void renameAccount(
        @RequestBody SingleNonNullablePropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.rename(request.getValue(), principal);
    }

    @DeleteMapping(value = "/account")
    public void deleteAccount(
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.delete(principal);
    }

    @GetMapping("/account")
    public GetAccountResponse getAccount(
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        AccountDto account = service.get(principal);
        return new GetAccountResponse(account.id(), account.name());
    }

    @GetMapping("/account/{id}")
    public GetAccountResponse getAccountById(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        AccountDto account = service.getById(id, principal);
        return new GetAccountResponse(account.id(), account.name());
    }

    @GetMapping("/account/groups")
    public AccountGroupsResponse getAccountGroups(
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        List<GroupDto> accountGroupList = service.getGroups(principal);
        return new AccountGroupsResponse(accountGroupList, accountGroupList.size());
    }

    @GetMapping("/account/group/{id}/lists")
    public AccountTaskListsResponse getAccountTaskLists(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        List<AccountTaskListResponse> taskLists = service.getTaskLists(id, principal).stream()
            .map(list -> new AccountTaskListResponse(list.safeId(), list.getName(), list.getGroup().getId()))
            .collect(Collectors.toList());
        return new AccountTaskListsResponse(taskLists, taskLists.size());
    }

    @GetMapping("/account/tasks/assigned")
    public GetAssignedTasksResponse getAccountAssignedTasks(
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        Set<Task> tasks = service.assignedTasks(principal);
        return new GetAssignedTasksResponse(tasks.stream()
            .map(task -> new GetAssignedTaskResponse(task.safeId(), task.getName(), task.getStatus()))
            .collect(Collectors.toSet()), tasks.size());
    }

    @GetMapping("/account/tasks/subscribed")
    public GetSubscribedTasksResponse getAccountSubscribedTasks(
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        Set<Task> tasks = service.subscribedTasks(principal);
        return new GetSubscribedTasksResponse(tasks.stream()
            .map(task -> new GetSubscribedTaskResponse(task.safeId(), task.getName(), task.getStatus()))
            .collect(Collectors.toSet()), tasks.size());
    }
}
