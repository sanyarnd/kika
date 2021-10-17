package kika.service.dto;

import kika.domain.Task;
import java.util.Set;

public record TaskDto(
    long id,
    String name,
    String description,
    Task.Status status,
    Long parentId,
    long listId,
    Set<Long> children
) {
}
