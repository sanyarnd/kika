package kika.controller;

import java.util.List;
import java.util.stream.Collectors;
import kika.controller.request.AddGroupMemberRequest;
import kika.controller.request.CreateGroupRequest;
import kika.controller.request.SetMemberRoleRequest;
import kika.controller.request.SetSingleNonNullableNumericPropertyRequest;
import kika.controller.request.SetSingleNonNullablePropertyRequest;
import kika.controller.response.GetGroupResponse;
import kika.controller.response.GroupMemberResponse;
import kika.controller.response.GroupMembersResponse;
import kika.controller.response.GroupTaskListResponse;
import kika.controller.response.GroupTaskListsResponse;
import kika.domain.Group;
import kika.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/group/create")
    public Long createGroup(@RequestBody CreateGroupRequest request) {
        return groupService.create(request.getName(), request.getOwnerId());
    }

    @GetMapping("/group/{id}")
    public GetGroupResponse getGroup(@PathVariable long id) {
        Group group = groupService.get(id);
        return new GetGroupResponse(group.getName(), group.getOwner().safeId(), group.getOwner().getName());
    }

    @PostMapping("/group/{id}/rename")
    public void renameGroup(@PathVariable long id, @RequestBody SetSingleNonNullablePropertyRequest request) {
        groupService.rename(id, request.getValue());
    }

    @DeleteMapping("/group/{id}")
    public void deleteGroup(@PathVariable long id) {
        groupService.delete(id);
    }

    @GetMapping("/group/{id}/lists")
    public GroupTaskListsResponse getGroupTaskLists(@PathVariable long id) {
        List<GroupTaskListResponse> taskLists = groupService.getTaskLists(id).stream()
            .map(list -> new GroupTaskListResponse(list.safeId(), list.getName()))
            .collect(Collectors.toList());
        return new GroupTaskListsResponse(taskLists, taskLists.size());
    }

    @PostMapping("/group/{groupId}/owner")
    public void transferGroupOwnership(
        @PathVariable long groupId,
        @RequestBody SetSingleNonNullableNumericPropertyRequest request
    ) {
        groupService.transferOwnership(groupId, request.getValue());
    }

    @GetMapping("/group/{id}/members")
    public GroupMembersResponse getGroupMembers(@PathVariable long id) {
        List<GroupMemberResponse> members = groupService.getMembers(id)
            .stream()
            .map(accountRole -> new GroupMemberResponse(accountRole.getId().getAccountId(), accountRole.getRole()))
            .collect(Collectors.toList());
        return new GroupMembersResponse(members, members.size());
    }

    @PostMapping("/group/{groupId}/member")
    public void addGroupMember(@PathVariable long groupId, @RequestBody AddGroupMemberRequest request) {
        groupService.addMember(groupId, request.getId(), request.getRole());
    }

    @DeleteMapping("/group/{groupId}/member/{memberId}")
    public void removeMember(@PathVariable long groupId, @PathVariable long memberId) {
        groupService.removeMember(groupId, memberId);
    }

    @PostMapping("/group/{groupId}/member/{memberId}/role")
    public void changeMemberRole(
        @PathVariable long groupId, @PathVariable long memberId,
        @RequestBody SetMemberRoleRequest request
    ) {
        groupService.changeMemberRole(groupId, memberId, request.getRole());
    }
}
