package kika.controller.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskListTasksResponse {
    private List<GetTaskListTaskResponse> tasks;
    private long count;
}
