package kika.service.dto;

import java.util.List;
import kika.controller.response.GetTaskListResponse;

public record TaskListDto(
    long id,
    String name,
    Long parent,
    long group,
    List<GetTaskListResponse> children,
    List<TaskDto> tasks
) {
}
