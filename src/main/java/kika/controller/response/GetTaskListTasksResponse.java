package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskListTasksResponse {
    private Set<GetTaskResponse> tasks;
    private long count;
}
