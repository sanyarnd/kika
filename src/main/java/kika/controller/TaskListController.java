package kika.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kika.controller.request.CreateTaskListRequest;
import kika.controller.request.NumericPropertyListRequest;
import kika.controller.request.SingleNonNullablePropertyRequest;
import kika.controller.response.GetTaskListResponse;
import kika.controller.response.GetTaskListTasksResponse;
import kika.controller.response.GetTaskResponse;
import kika.controller.response.TaskListAccountWithAccessResponse;
import kika.controller.response.TaskListAccountsWithAccessResponse;
import kika.security.principal.KikaPrincipal;
import kika.service.TaskListService;
import kika.service.dto.TaskDto;
import kika.service.dto.TaskListDto;
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
public class TaskListController {
    private final TaskListService service;

    @PostMapping("/list/create")
    public long createList(
        @RequestBody CreateTaskListRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return service.create(request.getName(), request.getParentId(), request.getGroupId(), principal);
    }

    @PostMapping("/list/{id}/rename")
    public void renameList(
        @PathVariable long id, @RequestBody SingleNonNullablePropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.rename(id, request.getValue(), principal);
    }

    @GetMapping("/list/{id}")
    public GetTaskListResponse get(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        TaskListDto taskList = service.get(id, principal);
        return new GetTaskListResponse(taskList.id(), taskList.name(), taskList.parent(), taskList.children());
    }

    @DeleteMapping("/list/{id}")
    public void deleteList(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.delete(id, principal);
    }

    @PostMapping("/list/{id}/accounts")
    public void setListAccess(
        @PathVariable long id, @RequestBody NumericPropertyListRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.setSpecialAccess(id, request.getValues(), principal);
    }

    @GetMapping("/list/{id}/accounts")
    public TaskListAccountsWithAccessResponse getAccountsWithAccess(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        List<TaskListAccountWithAccessResponse> accountsWithAccess =
            service.getAccountsWithAccess(id, principal).stream()
                .map(account -> new TaskListAccountWithAccessResponse(account.safeId(), account.getName()))
                .collect(Collectors.toList());
        return new TaskListAccountsWithAccessResponse(accountsWithAccess, accountsWithAccess.size());
    }

    @GetMapping("/list/{id}/tasks")
    public GetTaskListTasksResponse getTaskListTasks(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        Set<TaskDto> tasks = service.getTasks(id, principal);
        return new GetTaskListTasksResponse(tasks.stream()
            .map(task -> new GetTaskResponse(task.id(), task.name(), task.description(),
                task.status(), task.parentId(), task.listId(), task.children()))
            .collect(Collectors.toSet()), tasks.size());
    }
}
