package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAssignedTasksResponse {
    private Collection<GetAssignedTaskResponse> tasks;
    long count;
}
