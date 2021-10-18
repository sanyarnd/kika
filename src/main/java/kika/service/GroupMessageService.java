package kika.service;

import java.util.Collection;
import java.util.stream.Collectors;
import kika.domain.Group;
import kika.domain.GroupMessage;
import kika.repository.GroupRepository;
import kika.repository.GroupMessageRepository;
import kika.service.dto.GroupMessageDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupMessageService {
    private final GroupMessageRepository groupMessageRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public long create(long groupId, long memberId, @NotNull String body) {
        Group group = groupRepository.getById(groupId);
        if(group.getMembers().stream()
            .map(accountRole -> accountRole.getAccount().safeId())
            .noneMatch(id -> id == memberId)) {
            throw new IllegalArgumentException("Only members of the group can send group messages");
        }
        return groupMessageRepository.save(new GroupMessage(body, group)).safeId();
    }

    @Transactional
    public Collection<GroupMessageDto> getByGroup(long groupId) {
        return groupRepository.getById(groupId).getMessages().stream()
            .map(message -> new GroupMessageDto(message.safeId(), message.getGroup().safeId(), message.getCreatedDate(),
                message.getBody()))
            .collect(Collectors.toSet());
    }

    @Transactional
    public GroupMessageDto get(long id) {
        GroupMessage message = groupMessageRepository.getById(id);
        return new GroupMessageDto(message.safeId(), message.getGroup().safeId(), message.getCreatedDate(),
            message.getBody());
    }

    @Transactional
    public void delete(long messageId, long memberId) {
        GroupMessage message = groupMessageRepository.getById(messageId);
        if(message.getGroup().getMembers().stream()
            .map(accountRole -> accountRole.getAccount().safeId())
            .noneMatch(id -> id == memberId)) {
            throw new IllegalArgumentException("Only members of the group can delete group messages");
        }
        groupMessageRepository.delete(message);
    }
}
