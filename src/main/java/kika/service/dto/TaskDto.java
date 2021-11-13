package kika.service.dto;

import java.util.List;
import kika.domain.Task;

public record TaskDto(
    long id,
    String name,
    String description,
    Task.Status status,
    Long parentId,
    long listId,
    List<TaskDto> children
) {
}
