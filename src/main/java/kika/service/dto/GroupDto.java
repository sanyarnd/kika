package kika.service.dto;

import kika.domain.AccountRole;

public record GroupDto(long id, String name, AccountRole.Role role, long ownerId, String ownerName, long messageCount) {
}
