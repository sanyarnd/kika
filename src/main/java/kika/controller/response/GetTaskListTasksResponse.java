package kika.controller.response;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskListTasksResponse {
    private Set<GetTaskResponse> tasks;
    private long count;
}
