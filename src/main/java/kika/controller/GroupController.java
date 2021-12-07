package kika.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import kika.controller.request.AddGroupMemberRequest;
import kika.controller.request.CreateGroupRequest;
import kika.controller.request.EditGroupRequest;
import kika.controller.request.LoadMessagesRequest;
import kika.controller.request.SetMemberRoleRequest;
import kika.controller.request.SingleNonNullableNumericPropertyRequest;
import kika.controller.request.SingleNonNullablePropertyRequest;
import kika.controller.response.GetGroupEditInfoListsResponse;
import kika.controller.response.GetGroupEditInfoResponse;
import kika.controller.response.GetGroupInfoResponse;
import kika.controller.response.GetGroupMessageResponse;
import kika.controller.response.GetGroupMessagesResponse;
import kika.controller.response.GetGroupResponse;
import kika.controller.response.GetGroupTreeResponse;
import kika.controller.response.GetTaskListResponse;
import kika.controller.response.GroupMemberResponse;
import kika.controller.response.GroupMembersResponse;
import kika.controller.response.GroupTaskListsResponse;
import kika.controller.response.MessageBulk;
import kika.security.principal.KikaPrincipal;
import kika.service.GroupMessageService;
import kika.service.GroupService;
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
public class GroupController {
    private final GroupService groupService;
    private final GroupMessageService groupMessageService;

    @PostMapping("/group/create")
    public Long createGroup(
        @RequestBody @Valid CreateGroupRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return groupService.create(request.name(), request.members(), principal);
    }

    @GetMapping("/group/{id}")
    public GetGroupResponse getGroup(@PathVariable long id, @AuthenticationPrincipal KikaPrincipal principal) {
        GroupDto group = groupService.get(id, principal);
        return new GetGroupResponse(group.id(), group.name(), group.ownerId(), group.ownerName(), group.role(),
            group.messageCount());
    }

    @GetMapping("/group/{id}/info/edit")
    public GetGroupEditInfoResponse getGroupEditInfo(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return groupService.getEditInfo(id, principal);
    }

    @GetMapping("/group/{id}/info/lists")
    public GetGroupEditInfoListsResponse getGroupEditInfoLists(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return groupService.getEditInfoLists(id, principal);
    }

    @GetMapping("/group/{groupId}/tree/list/{listId}")
    public GetGroupTreeResponse getGroupListTree(
        @PathVariable long groupId, @PathVariable long listId, @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return groupService.getTree(groupId, listId, principal);
    }

    @GetMapping("/group/{groupId}/tree/task/{taskId}")
    public GetGroupTreeResponse getListTaskTree(
        @PathVariable long groupId, @PathVariable long taskId, @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return groupService.getTreeWithTasks(groupId, taskId, principal);
    }

    @PostMapping("/group/{id}/rename")
    public void renameGroup(
        @PathVariable long id,
        @RequestBody @Valid SingleNonNullablePropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        groupService.rename(id, request.value(), principal);
    }

    @DeleteMapping("/group/{id}")
    public void deleteGroup(@PathVariable long id, @AuthenticationPrincipal KikaPrincipal principal) {
        groupService.delete(id, principal);
    }

    @GetMapping("/group/{id}/lists")
    public GroupTaskListsResponse getGroupTaskLists(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        List<GetTaskListResponse> taskLists = groupService.getTaskLists(id, principal);
        return new GroupTaskListsResponse(taskLists, taskLists.size());
    }

    @PostMapping("/group/{groupId}/owner")
    public void transferGroupOwnership(
        @PathVariable long groupId,
        @RequestBody @Valid SingleNonNullableNumericPropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        groupService.transferOwnership(groupId, request.value(), principal);
    }

    @GetMapping("/group/{id}/members")
    public GroupMembersResponse getGroupMembers(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        List<GroupMemberResponse> members = groupService.getMembers(id, principal)
            .stream()
            .map(accountRole -> new GroupMemberResponse(accountRole.getId().getAccountId(), accountRole.getRole(),
                accountRole.getAccount().getName()))
            .collect(Collectors.toList());
        return new GroupMembersResponse(members, members.size());
    }

    @PostMapping("/group/{groupId}/member")
    public void addGroupMember(
        @PathVariable long groupId,
        @RequestBody @Valid AddGroupMemberRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        groupService.addMember(groupId, request.getId(), request.getRole(), principal);
    }

    @DeleteMapping("/group/{groupId}/member/{memberId}")
    public void removeMember(
        @PathVariable long groupId,
        @PathVariable long memberId,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        groupService.removeMember(groupId, memberId, principal);
    }

    @PostMapping("/group/{groupId}/member/{memberId}/role")
    public void changeMemberRole(
        @PathVariable long groupId,
        @PathVariable long memberId,
        @RequestBody @Valid SetMemberRoleRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        groupService.changeMemberRole(groupId, memberId, request.role(), principal);
    }

    @GetMapping("/group/{groupId}/messages")
    public GetGroupMessagesResponse getGroupMessages(
        @PathVariable long groupId,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        List<GetGroupMessageResponse> notifications = groupMessageService.getByGroup(groupId, principal).stream()
            .map(message -> new GetGroupMessageResponse(message.id(), message.groupId(),
                message.createdDate(), message.body(), message.sender()))
            .sorted((o1, o2) -> Long.compare(o2.getId(), o1.getId()))
            .collect(Collectors.toList());
        return new GetGroupMessagesResponse(notifications, notifications.size());
    }

    @PostMapping("/group/{id}/edit")
    public void addGroupMembers(
        @PathVariable long id,
        @RequestBody @Valid EditGroupRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        groupService.edit(id, request.name(), request.members(), principal);
    }

    @PostMapping("/group/{id}/info")
    public GetGroupInfoResponse getGroupInfo(
        @PathVariable long id,
        @RequestBody LoadMessagesRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return groupService.getInfo(id, request.offset(), request.count(), principal);
    }

    @PostMapping("/group/{groupId}/messages")
    public MessageBulk getGroupMessages(
        @PathVariable long groupId,
        @RequestBody LoadMessagesRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return groupMessageService.getByGroup(groupId, request.offset(), request.count(), principal);
    }

    @DeleteMapping("/group/{groupId}/messages")
    public void clearGroupMessages(
        @PathVariable long groupId,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        groupService.deleteByGroup(groupId, principal);
    }
}
