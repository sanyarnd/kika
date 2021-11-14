package kika.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import kika.controller.request.CreateTaskRequest;
import kika.controller.request.EditTaskRequest;
import kika.controller.request.MoveTaskRequest;
import kika.controller.request.SetTaskStatusRequest;
import kika.controller.request.SingleNonNullablePropertyRequest;
import kika.controller.request.SingleNullableStringPropertyRequest;
import kika.controller.response.GetTaskAssigneeResponse;
import kika.controller.response.GetTaskAssigneesResponse;
import kika.controller.response.GetTaskResponse;
import kika.controller.response.GetTaskSubscriberResponse;
import kika.controller.response.GetTaskSubscribersResponse;
import kika.domain.Account;
import kika.security.principal.KikaPrincipal;
import kika.service.TaskService;
import kika.service.dto.TaskDto;
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
public class TaskController {
    private final TaskService service;

    @PostMapping("/task/create")
    public long createTask(
        @RequestBody @Valid CreateTaskRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return service.create(request.name(),
            request.description(),
            request.listId(),
            request.parentId(),
            principal);
    }

    @PostMapping("/task/{id}/rename")
    public void renameTask(
        @PathVariable long id, @RequestBody @Valid SingleNonNullablePropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.rename(id, request.value(), principal);
    }

    @PostMapping("/task/{id}/description")
    public void setTaskDescription(
        @PathVariable long id, @RequestBody @Valid SingleNullableStringPropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.setDescription(id, request.value(), principal);
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

    @GetMapping("/task/{id}")
    public GetTaskResponse getTask(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        TaskDto task = service.get(id, principal);
        return new GetTaskResponse(task.id(), task.name(), task.description(), task.status(), task.parentId(),
            task.listId(), getFullChildrenTree(task));
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
        @PathVariable long id, @RequestBody @Valid MoveTaskRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.move(id, request.listId(), request.parentId(), principal);
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
        @PathVariable long id, @RequestBody @Valid SetTaskStatusRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.setStatus(id, request.status(), principal);
    }

    @PostMapping("/task/{id}/edit")
    public void editTask(
        @PathVariable long id,
        @RequestBody @Valid EditTaskRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        service.edit(id, request, principal);
    }
}
