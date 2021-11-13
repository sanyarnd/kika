package kika.controller.response;

import java.util.List;
import kika.service.dto.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskListResponse {
    private long id;
    private String name;
    private Long parentId;
    private long groupId;
    private List<GetTaskListResponse> children;
    private List<TaskDto> tasks;
}
