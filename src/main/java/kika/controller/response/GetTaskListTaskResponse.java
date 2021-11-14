package kika.controller.response;

import java.util.List;
import kika.domain.Task;
import kika.service.dto.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskListTaskResponse {
    private long id;
    private String name;
    private String description;
    private Task.Status status;
    private Long parentId;
    private long listId;
    private List<TaskDto> children;
}
