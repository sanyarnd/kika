package kika.service.dto;

import java.time.Instant;

public record GroupMessageDto(
    long id,
    long groupId,
    Instant createdDate,
    String body,
    String sender
) {
}
