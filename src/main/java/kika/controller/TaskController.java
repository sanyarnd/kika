package kika.controller;

import kika.controller.request.*;
import kika.controller.response.*;
import kika.domain.*;
import kika.service.TaskService;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class TaskController {
    private final TaskService service;

    @PostMapping("/task/create")
    public long createTask(@RequestBody CreateTaskRequest request) {
        return service.create(request.getName(), request.getDescription(), request.getListId(), request.getParentId());
    }

    @PostMapping("/task/{id}/rename")
    public void renameTask(@PathVariable long id, @RequestBody SetSingleNonNullablePropertyRequest request) {
        service.rename(id, request.getValue());
    }

    @PostMapping("/task/{id}/description")
    public void setTaskDescription(@PathVariable long id, @RequestBody SetSingleNullableStringPropertyRequest request) {
        service.setDescription(id, request.getValue());
    }

    @GetMapping("/task/{id}")
    public GetTaskResponse getTask(@PathVariable long id) {
        TaskDto task = service.get(id);
        return new GetTaskResponse(task.id(), task.name(), task.description(), task.status(), task.parentId(),
                task.listId(), task.children());
    }

    @DeleteMapping("/task/{id}")
    public void deleteTask(@PathVariable long id) {
        service.delete(id);
    }

    @PostMapping("/task/{id}/move")
    public void move(@PathVariable long id, @RequestBody MoveTaskRequest request) {
        service.move(id, request.getListId(), request.getParentId());
    }

    @PostMapping("/task/{id}/assignees")
    public void assign(@PathVariable long id, @RequestBody SetNumericPropertyListRequest request) {
        service.assign(id, request.getValues());
    }

    @GetMapping("/task/{id}/assignees")
    public GetTaskAssigneesResponse getTaskAssignees(@PathVariable long id) {
        Collection<Account> assignees = service.getAssignees(id);
        return new GetTaskAssigneesResponse(assignees.stream()
                .map(account -> new GetTaskAssigneeResponse(account.safeId(), account.getName()))
                .collect(Collectors.toSet()), assignees.size());
    }

    @PostMapping("/task/{id}/subscribers")
    public void subscribe(@PathVariable long id, @RequestBody SetNumericPropertyListRequest request) {
        service.subscribe(id, request.getValues());
    }

    @GetMapping("/task/{id}/subscribers")
    public GetTaskSubscribersResponse getTaskSubscribers(@PathVariable long id) {
        Collection<Account> subscribers = service.getSubscribers(id);
        return new GetTaskSubscribersResponse(subscribers.stream()
                .map(account -> new GetTaskSubscriberResponse(account.safeId(), account.getName()))
                .collect(Collectors.toSet()), subscribers.size());
    }

    @PostMapping("/task/{id}/status")
    public void setTaskStatus(@PathVariable long id, @RequestBody SetTaskStatusRequest request) {
        service.setStatus(id, request.getStatus());
    }
}
