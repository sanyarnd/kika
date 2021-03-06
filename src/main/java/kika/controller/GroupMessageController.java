package kika.controller;

import javax.validation.Valid;
import kika.controller.request.SingleBigNonNullablePropertyRequest;
import kika.controller.response.GetGroupMessageResponse;
import kika.security.principal.KikaPrincipal;
import kika.service.GroupMessageService;
import kika.service.dto.GroupMessageDto;
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
public class GroupMessageController {
    private final GroupMessageService service;

    @PostMapping("/group/{groupId}/message")
    public long createGroupMessage(
        @PathVariable long groupId,
        @RequestBody @Valid SingleBigNonNullablePropertyRequest request,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        return service.create(groupId, request.value(), principal);
    }

    @GetMapping("/message/{id}")
    public GetGroupMessageResponse getGroupMessage(
        @PathVariable long id,
        @AuthenticationPrincipal KikaPrincipal principal
    ) {
        GroupMessageDto message = service.get(id, principal);
        return new GetGroupMessageResponse(message.id(), message.groupId(), message.createdDate(), message.body(),
            message.sender());
    }

    @DeleteMapping("/message/{messageId}")
    public void deleteGroupMessage(@PathVariable long messageId, @AuthenticationPrincipal KikaPrincipal principal) {
        service.delete(messageId, principal);
    }
}
