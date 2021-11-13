package kika.controller.response;

import java.util.List;
import kika.service.dto.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupTaskListResponse {
    private long id;
    private String name;
    private List<GroupTaskListResponse> children;
    private List<TaskDto> tasks;
}
