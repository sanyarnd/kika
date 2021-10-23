package kika.controller;

import kika.controller.request.*;
import kika.controller.response.*;
import kika.domain.Account;
import kika.security.principal.KikaPrincipal;
import kika.service.TaskService;
import kika.service.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class TaskController {
    private final TaskService service;

    @PostMapping("/task/create")
    public long createTask(
        @RequestBody CreateTaskRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return service.create(request.getName(), request.getDescription(), request.getListId(), request.getParentId(), principal);
    }

    @PostMapping("/task/{id}/rename")
    public void renameTask(
        @PathVariable long id, @RequestBody SingleNonNullablePropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.rename(id, request.getValue(), principal);
    }

    @PostMapping("/task/{id}/description")
    public void setTaskDescription(
        @PathVariable long id, @RequestBody SingleNullableStringPropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.setDescription(id, request.getValue(), principal);
    }

    @GetMapping("/task/{id}")
    public GetTaskResponse getTask(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        TaskDto task = service.get(id, principal);
        return new GetTaskResponse(task.id(), task.name(), task.description(), task.status(), task.parentId(),
            task.listId(), task.children());
    }

    @DeleteMapping("/task/{id}")
    public void deleteTask(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.delete(id, principal);
    }

    @PostMapping("/task/{id}/move")
    public void move(
        @PathVariable long id, @RequestBody MoveTaskRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.move(id, request.getListId(), request.getParentId(), principal);
    }

    @PostMapping("/task/{id}/assignee")
    public void assign(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.assign(id, principal);
    }

    @DeleteMapping("/task/{id}/assignee")
    public void retract(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.retract(id, principal);
    }

    @GetMapping("/task/{id}/assignees")
    public GetTaskAssigneesResponse getTaskAssignees(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        Collection<Account> assignees = service.getAssignees(id, principal);
        return new GetTaskAssigneesResponse(assignees.stream()
            .map(account -> new GetTaskAssigneeResponse(account.safeId(), account.getName()))
            .collect(Collectors.toSet()), assignees.size());
    }

    @PostMapping("/task/{id}/subscriber")
    public void subscribe(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.subscribe(id, principal);
    }

    @DeleteMapping("/task/{id}/subscriber")
    public void unsubscribe(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.unsubscribe(id, principal);
    }

    @GetMapping("/task/{id}/subscribers")
    public GetTaskSubscribersResponse getTaskSubscribers(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        Collection<Account> subscribers = service.getSubscribers(id, principal);
        return new GetTaskSubscribersResponse(subscribers.stream()
            .map(account -> new GetTaskSubscriberResponse(account.safeId(), account.getName()))
            .collect(Collectors.toSet()), subscribers.size());
    }

    @PostMapping("/task/{id}/status")
    public void setTaskStatus(
        @PathVariable long id, @RequestBody SetTaskStatusRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.setStatus(id, request.getStatus(), principal);
    }
}
