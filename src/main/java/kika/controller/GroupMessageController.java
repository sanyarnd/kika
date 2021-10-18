package kika.controller;

import java.util.Set;
import java.util.stream.Collectors;
import kika.controller.request.SingleBigNonNullablePropertyRequest;
import kika.controller.response.GetGroupMessageResponse;
import kika.controller.response.GetGroupMessagesResponse;
import kika.domain.GroupMessage;
import kika.service.GroupMessageService;
import kika.service.dto.GroupMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class GroupMessageController {
    private final GroupMessageService service;

    @PostMapping("/group/{groupId}/member/{memberId}/message")
    public long createGroupMessage(@PathVariable long groupId, @PathVariable long memberId, @RequestBody SingleBigNonNullablePropertyRequest request) {
        return service.create(groupId, memberId, request.getValue());
    }

    @GetMapping("/message/{id}")
    public GetGroupMessageResponse getGroupMessage(@PathVariable long id) {
        GroupMessageDto message = service.get(id);
        return new GetGroupMessageResponse(message.id(), message.groupId(), message.createdDate(), message.body());
    }

    @DeleteMapping("/message/{messageId}/account/{accountId}")
    public void deleteGroupMessage(@PathVariable long messageId, @PathVariable long accountId) {
        service.delete(messageId, accountId);
    }
}
