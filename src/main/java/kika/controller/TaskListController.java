package kika.controller;

import kika.controller.request.CreateTaskListRequest;
import kika.controller.request.SetNumericPropertyListRequest;
import kika.controller.request.SetSingleNonNullablePropertyRequest;
import kika.controller.response.*;
import kika.domain.AutoPersistable;
import kika.domain.TaskList;
import kika.service.TaskListService;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TaskListController {
    private final TaskListService service;

    @PostMapping("/list/create")
    public long createList(@RequestBody CreateTaskListRequest request) {
        return service.create(request.getName(), request.getParentId(), request.getGroupId());
    }

    @PostMapping("/list/{id}/rename")
    public void renameList(@PathVariable long id, @RequestBody SetSingleNonNullablePropertyRequest request) {
        service.rename(id, request.getValue());
    }

    @GetMapping("/list/{id}")
    public GetTaskListResponse get(@PathVariable long id) {
        TaskList taskList = service.get(id);
        return new GetTaskListResponse(taskList.safeId(), taskList.getName(),
                taskList.getParentId(), taskList.getChildren().stream().map(TaskList::safeId).collect(Collectors.toSet()));
    }

    @DeleteMapping("/list/{id}")
    public void deleteList(@PathVariable long id) {
        service.delete(id);
    }

    @PostMapping("/list/{id}/accounts")
    public void setListAccess(@PathVariable long id, @RequestBody SetNumericPropertyListRequest request) {
        service.setSpecialAccess(id, request.getValues());
    }

    @GetMapping("/list/{id}/accounts")
    public TaskListAccountsWithAccessResponse getAccountsWithAccess(@PathVariable long id) {
        List<TaskListAccountWithAccessResponse> accountsWithAccess = service.getAccountsWithAccess(id).stream()
                .map(account -> new TaskListAccountWithAccessResponse(account.safeId(), account.getName()))
                .collect(Collectors.toList());
        return new TaskListAccountsWithAccessResponse(accountsWithAccess, accountsWithAccess.size());
    }

    @GetMapping("/list/{id}/tasks")
    public GetTaskListTasksResponse getTaskListTasks(@PathVariable long id) {
        Set<TaskDto> tasks = service.getTasks(id);
        return new GetTaskListTasksResponse(tasks.stream()
                .map(task -> new GetTaskResponse(task.id(), task.name(), task.description(),
                        task.status(), task.parentId(), task.listId(), task.children()))
                .collect(Collectors.toSet()), tasks.size());
    }
}
