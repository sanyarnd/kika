package kika.service.dto;

import kika.controller.response.TaskListSpecialAccessResponse;
import java.util.List;

public record ConciseTaskListDto (
    long id,
    String name,
    List<ConciseTaskListDto> children,
    List<ConciseTaskDto> tasks
) {}
