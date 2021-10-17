package kika.controller.response;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAssignedTasksResponse {
    private Collection<GetAssignedTaskResponse> tasks;
    private long count;
}
