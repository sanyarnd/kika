package kika.service;

import java.util.Collection;
import java.util.stream.Collectors;
import kika.domain.Group;
import kika.domain.GroupMessage;
import kika.repository.AccountRepository;
import kika.repository.GroupMessageRepository;
import kika.repository.GroupRepository;
import kika.security.principal.KikaPrincipal;
import kika.service.dto.GroupMessageDto;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupMessageService {
    private final GroupMessageRepository groupMessageRepository;
    private final GroupRepository groupRepository;
    private final AccountRepository accountRepository;

    private void checkMemberAccess(KikaPrincipal principal, Group group) {
        if (group.getMembers().stream()
            .map(accountRole -> accountRole.getAccount().safeId())
            .noneMatch(memberId -> memberId == principal.accountId())) {
            throw new BadCredentialsException("Access denied");
        }
    }

    @Transactional
    public long create(long groupId, @NotNull String body, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkMemberAccess(principal, group);
        if (group.getMembers().stream()
            .map(accountRole -> accountRole.getAccount().safeId())
            .noneMatch(id -> id == principal.accountId())) {
            throw new IllegalArgumentException("Only members of the group can send group messages");
        }
        return groupMessageRepository.save(new GroupMessage(body, group)).safeId();
    }

    @Transactional
    public Collection<GroupMessageDto> getByGroup(long groupId, KikaPrincipal principal) {
        Group group = groupRepository.getById(groupId);
        checkMemberAccess(principal, group);
        return group.getMessages().stream()
            .map(message -> new GroupMessageDto(message.safeId(), message.getGroup().safeId(), message.getCreatedDate(),
                message.getBody(), accountRepository.getById(Long.valueOf(message.getCreatedBy())).getName()))
            .collect(Collectors.toSet());
    }

    @Transactional
    public GroupMessageDto get(long id, KikaPrincipal principal) {
        GroupMessage message = groupMessageRepository.getById(id);
        checkMemberAccess(principal, message.getGroup());
        return new GroupMessageDto(message.safeId(), message.getGroup().safeId(), message.getCreatedDate(),
            message.getBody(), accountRepository.getById(Long.valueOf(message.getCreatedBy())).getName());
    }

    @Transactional
    public void delete(long messageId, KikaPrincipal principal) {
        GroupMessage message = groupMessageRepository.getById(messageId);
        checkMemberAccess(principal, message.getGroup());
        if (message.getGroup().getMembers().stream()
            .map(accountRole -> accountRole.getAccount().safeId())
            .noneMatch(id -> id == principal.accountId())) {
            throw new IllegalArgumentException("Only members of the group can delete group messages");
        }
        groupMessageRepository.delete(message);
    }
}
