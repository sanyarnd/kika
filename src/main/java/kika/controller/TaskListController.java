package kika.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import kika.controller.request.CreateTaskListRequest;
import kika.controller.request.EditTaskListRequest;
import kika.controller.request.MoveListRequest;
import kika.controller.request.NumericPropertyListRequest;
import kika.controller.request.SingleNonNullablePropertyRequest;
import kika.controller.response.GetTaskListResponse;
import kika.controller.response.GetTaskListTasksResponse;
import kika.controller.response.GetTaskResponse;
import kika.controller.response.TaskListAccountsWithAccessResponse;
import kika.controller.response.TaskListSpecialAccessResponse;
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
        @RequestBody @Valid CreateTaskListRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return service.create(request.name(), request.parentId(), request.groupId(), request.accessList(),
            principal);
    }

    @PostMapping("/list/{id}/rename")
    public void renameList(
        @PathVariable long id,
        @RequestBody @Valid SingleNonNullablePropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.rename(id, request.value(), principal);
    }

    @GetMapping("/list/{id}")
    public GetTaskListResponse get(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        TaskListDto taskList = service.get(id, principal);
        return new GetTaskListResponse(taskList.id(), taskList.name(), taskList.parent(), taskList.group(),
            taskList.children(), taskList.tasks());
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
        @PathVariable long id,
        @RequestBody @Valid NumericPropertyListRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.setSpecialAccess(id, request.values(), principal);
    }

    @GetMapping("/list/{id}/accounts")
    public TaskListAccountsWithAccessResponse getAccountsWithAccess(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        TaskListSpecialAccessResponse accountAccess = service.getAccountsWithAccess(id, principal);
        return new TaskListAccountsWithAccessResponse(accountAccess.isSet(), accountAccess.getAccounts(),
            accountAccess.getAccounts().size());
    }

    private List<GetTaskResponse> getFullChildrenTree(TaskDto task) {
        if (task.children().isEmpty()) {
            return List.of();
        }
        return task.children().stream()
            .map(child -> new GetTaskResponse(child.id(),
                child.name(),
                child.description(),
                child.status(),
                child.parentId(),
                child.listId(),
                getFullChildrenTree(child)))
            .toList();
    }

    @GetMapping("/list/{id}/tasks")
    public GetTaskListTasksResponse getTaskListTasks(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        Set<TaskDto> tasks = service.getTasks(id, principal);
        return new GetTaskListTasksResponse(tasks.stream()
            .map(task -> new GetTaskResponse(task.id(), task.name(), task.description(),
                task.status(), task.parentId(), task.listId(), getFullChildrenTree(task)))
            .collect(Collectors.toSet()), tasks.size());
    }

    @PostMapping("list/{id}/edit")
    public void editList(
        @PathVariable long id,
        @RequestBody @Valid EditTaskListRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.edit(id, request, principal);
    }

    @PostMapping("list/{id}/move")
    public void editList(
        @PathVariable long id,
        @RequestBody @Valid MoveListRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.move(id, request.parentId(), principal);
    }
}
