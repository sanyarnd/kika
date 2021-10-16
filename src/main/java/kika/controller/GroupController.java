package kika.controller;

import kika.controller.request.*;
import kika.controller.response.*;
import kika.domain.Group;
import kika.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public void transferGroupOwnership(@PathVariable long groupId, @RequestBody SetSingleNonNullableNumericPropertyRequest request) {
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
    public void changeMemberRole(@PathVariable long groupId, @PathVariable long memberId,
                                 @RequestBody SetMemberRoleRequest request) {
        groupService.changeMemberRole(groupId, memberId, request.getRole());
    }
}
