package kika.controller;

import java.util.Collection;
import java.util.stream.Collectors;
import kika.controller.request.CreateTaskRequest;
import kika.controller.request.MoveTaskRequest;
import kika.controller.request.NumericPropertyListRequest;
import kika.controller.request.SingleNonNullablePropertyRequest;
import kika.controller.request.SingleNullableStringPropertyRequest;
import kika.controller.request.SetTaskStatusRequest;
import kika.controller.response.GetTaskAssigneeResponse;
import kika.controller.response.GetTaskAssigneesResponse;
import kika.controller.response.GetTaskResponse;
import kika.controller.response.GetTaskSubscriberResponse;
import kika.controller.response.GetTaskSubscribersResponse;
import kika.domain.Account;
import kika.service.TaskService;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TaskController {
    private final TaskService service;

    @PostMapping("/task/create")
    public long createTask(@RequestBody CreateTaskRequest request) {
        return service.create(request.getName(), request.getDescription(), request.getListId(), request.getParentId());
    }

    @PostMapping("/task/{id}/rename")
    public void renameTask(@PathVariable long id, @RequestBody SingleNonNullablePropertyRequest request) {
        service.rename(id, request.getValue());
    }

    @PostMapping("/task/{id}/description")
    public void setTaskDescription(@PathVariable long id, @RequestBody SingleNullableStringPropertyRequest request) {
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
    public void assign(@PathVariable long id, @RequestBody NumericPropertyListRequest request) {
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
    public void subscribe(@PathVariable long id, @RequestBody NumericPropertyListRequest request) {
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
