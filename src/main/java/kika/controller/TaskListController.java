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
import kika.service.TaskListService;
import kika.service.dto.TaskDto;
import kika.service.dto.TaskListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TaskListController {
    private final TaskListService service;

    @PostMapping("/list/create")
    public long createList(@RequestBody CreateTaskListRequest request) {
        return service.create(request.getName(), request.getParentId(), request.getGroupId());
    }

    @PostMapping("/list/{id}/rename")
    public void renameList(@PathVariable long id, @RequestBody SingleNonNullablePropertyRequest request) {
        service.rename(id, request.getValue());
    }

    @GetMapping("/list/{id}")
    public GetTaskListResponse get(@PathVariable long id) {
        TaskListDto taskList = service.get(id);
        return new GetTaskListResponse(taskList.id(), taskList.name(), taskList.parent(), taskList.children());
    }

    @DeleteMapping("/list/{id}")
    public void deleteList(@PathVariable long id) {
        service.delete(id);
    }

    @PostMapping("/list/{id}/accounts")
    public void setListAccess(@PathVariable long id, @RequestBody NumericPropertyListRequest request) {
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
