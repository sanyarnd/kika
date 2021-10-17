package kika.service.dto;

import java.util.Set;

public record TaskListDto(
    long id,
    String name,
    Long parent,
    long group,
    Set<Long> children,
    Set<Long> tasks
) {
}
